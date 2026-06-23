package com.fintech.b2b.application.service;

import com.fintech.b2b.application.port.in.RealizarTransferenciaCommand;
import com.fintech.b2b.application.port.in.RealizarTransferenciaUseCase;
import com.fintech.b2b.domain.model.Billetera;
import com.fintech.b2b.domain.model.Transaccion;
import com.fintech.b2b.domain.model.exception.BilleteraNoEncontradaException;
import com.fintech.b2b.domain.model.exception.OperacionInvalidaException;
import com.fintech.b2b.domain.model.port.BilleteraRepositoryPort;
import com.fintech.b2b.domain.model.port.TransaccionRepositoryPort;
import com.fintech.b2b.domain.model.port.UsuarioAutenticadoPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class RealizarTransferenciaUseCaseImpl implements RealizarTransferenciaUseCase {

    private final BilleteraRepositoryPort billeteraRepositoryPort;
    private final TransaccionRepositoryPort transaccionRepositoryPort;
    private final UsuarioAutenticadoPort usuarioAutenticadoPort;

    public RealizarTransferenciaUseCaseImpl(BilleteraRepositoryPort billeteraRepositoryPort,
                                           TransaccionRepositoryPort transaccionRepositoryPort,
                                           UsuarioAutenticadoPort usuarioAutenticadoPort) {
        this.billeteraRepositoryPort = billeteraRepositoryPort;
        this.transaccionRepositoryPort = transaccionRepositoryPort;
        this.usuarioAutenticadoPort = usuarioAutenticadoPort;
    }

    @Override
    @Transactional // 🔥 LÍNEA DE DEFENSA 4: Garantiza atomicidad absoluta (Todo o Nada)
    public Transaccion ejecutar(RealizarTransferenciaCommand command) {
        
        // 1. CONTROL DE IDEMPOTENCIA: Verificar si la transacción ya fue procesada
        // Evita que clicks duplicados del cliente generen transferencias repetidas
    	if (transaccionRepositoryPort.buscarPorCorrelationId(command.idempotencyKey().toString()).isPresent()) {
            throw new OperacionInvalidaException("La transferencia con la llave de idempotencia provista ya fue procesada anteriormente.");
        }

        // 2. BLINDAJE ANTI-IDOR: Extraer la identidad del emisor directo desde el token en RAM
        Long usuarioOrigenId = usuarioAutenticadoPort.getIdUsuarioActual();

        // 3. BUSCAR BILLETERA ORIGEN (Asegurando que pertenezca al usuario autenticado)
        Billetera billeteraOrigen = billeteraRepositoryPort.buscarPorUsuarioId(usuarioOrigenId)
                .orElseThrow(() -> new BilleteraNoEncontradaException("No se encontró una billetera activa para el usuario autenticado."));

        // 4. BUSCAR BILLETERA DESTINO
        Billetera billeteraDestino = billeteraRepositoryPort.buscarPorId(command.billeteraDestinoId())
                .orElseThrow(() -> new BilleteraNoEncontradaException("La billetera de destino no existe en el sistema."));

        // Regla de negocio básica: Evitar transferencias a la misma cuenta
        if (billeteraOrigen.getId().equals(billeteraDestino.getId())) {
            throw new OperacionInvalidaException("Operación inválida: No es posible realizar transferencias hacia la misma cuenta de origen.");
        }

        // 5. INICIAR ENTIDAD DE DOMINIO (Nace en estado PENDING y valida montos positivos)
        Transaccion transaccion = Transaccion.crear(
                billeteraOrigen.getId(),
                billeteraDestino.getId(),
                command.monto(),
                command.concepto(),
                command.idempotencyKey()
        );

        // 6. EJECUTAR LÓGICA DE NEGOCIO (Mutación de estados e invariantes en memoria RAM)
        // Si el saldo es insuficiente, aquí estallará SaldoInsuficienteException abortando todo
        billeteraOrigen.debitar(command.monto());
        billeteraDestino.acreditar(command.monto());

        // 7. GUARDAR CAMBIOS DE SALDOS (Persistencia)
        // Al ejecutar flush() en el adaptador, el @Version de JPA validará que nadie haya
        // modificado las cuentas concurrentemente. Si hay colisión, estalla ConcurrenciaException.
        billeteraRepositoryPort.guardar(billeteraOrigen);
        billeteraRepositoryPort.guardar(billeteraDestino);

        // 8. TRANSICIONAR ESTADO DEL EVENTO
        transaccion.completar();

        // 9. ASENTAR EN EL LEDGER INMUTABLE (Inserta la cabecera y los movimientos de partida doble)
        return transaccionRepositoryPort.guardar(transaccion);
    }
}
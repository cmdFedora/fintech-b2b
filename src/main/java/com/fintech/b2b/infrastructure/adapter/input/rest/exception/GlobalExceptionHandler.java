package com.fintech.b2b.infrastructure.adapter.input.rest.exception;

import com.fintech.b2b.domain.model.exception.*;
import com.fintech.b2b.domain.model.exception.ConcurrenciaException; // Ajustado a tu paquete de persistencia
import com.fintech.b2b.infrastructure.adapter.input.rest.dto.ApiError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Captura colisiones de Bloqueo Optimista (Double Spending mitigado en persistencia).
     * Retorna HTTP 409 Conflict.
     */
    @ExceptionHandler(ConcurrenciaException.class)
    public ResponseEntity<ApiError> handleConcurrencia(ConcurrenciaException ex, HttpServletRequest request) {
        log.warn("Conflicto de concurrencia detectado en la ruta: {} - Mensaje: {}", request.getRequestURI(), ex.getMessage());
        
        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Captura reglas financieras violadas (Saldo Insuficiente).
     * Retorna HTTP 422 Unprocessable Entity (Entidad semánticamente incorrecta).
     */
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ApiError> handleSaldoInsuficiente(SaldoInsuficienteException ex, HttpServletRequest request) {
        log.warn("Operación financiera rechazada: {} - Ruta: {}", ex.getMessage(), request.getRequestURI());

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                HttpStatus.UNPROCESSABLE_CONTENT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(error);
    }

    /**
     * Captura montos inválidos (ej. valores negativos o cero).
     * Retorna HTTP 422 Unprocessable Entity.
     */
    @ExceptionHandler(SaldoInvalidoException.class)
    public ResponseEntity<ApiError> handleSaldoInvalido(SaldoInvalidoException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                HttpStatus.UNPROCESSABLE_CONTENT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(error);
    }

    /**
     * Captura intentos de operar con billeteras inexistentes.
     * Retorna HTTP 404 Not Found.
     */
    @ExceptionHandler(BilleteraNoEncontradaException.class)
    public ResponseEntity<ApiError> handleBilleteraNoEncontrada(BilleteraNoEncontradaException ex, HttpServletRequest request) {
        log.error("Error de búsqueda: {} en la ruta: {}", ex.getMessage(), request.getRequestURI());

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Captura infracciones de seguridad e IDOR (Intentos de mover dinero de billeteras ajenas).
     * Retorna HTTP 403 Forbidden.
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ApiError> handleAccesoDenegado(AccesoDenegadoException ex, HttpServletRequest request) {
        log.error("ALERTA DE SEGURIDAD: Intento de acceso no autorizado en la ruta: {} - Mensaje: {}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Captura cualquier otra excepción de negocio genérica heredada de BusinessException.
     * Retorna HTTP 400 Bad Request.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleGenericBusinessError(BusinessException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * El último cinturón de seguridad: errores imprevistos del servidor (NullPointerException, caídas de red, etc.).
     * Retorna HTTP 500 Internal Server Error. No expone detalles técnicos sensibles al cliente por seguridad.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleFallbackFatal(Exception ex, HttpServletRequest request) {
        // Logeamos el stacktrace completo internamente para depuración de los desarrolladores
        log.error("ERROR CRÍTICO NO CONTROLADO en ruta: {}", request.getRequestURI(), ex);

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ha ocurrido un error interno en el servidor. Por favor, póngase en contacto con el administrador del sistema.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Captura las validaciones fallidas de los DTOs de entrada (@Valid).
     * Retorna HTTP 400 Bad Request con el mensaje exacto de la infracción.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Extraemos el mensaje de error del primer campo que haya fallado 
        // (ej. "El monto debe ser un valor positivo mayor a cero.")
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                errorMessage != null ? errorMessage : "Error de validación en los datos enviados.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
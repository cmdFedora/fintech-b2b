# 🏦 B2B Fintech API

Una API REST transaccional robusta para operaciones financieras Business-to-Business (B2B), construida con un enfoque estricto en el diseño y la seguridad.

## 🛠️ Stack Tecnológico
* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3.4
* **Base de Datos:** PostgreSQL
* **Migraciones:** Flyway
* **Seguridad:** Spring Security + JWT
* **Documentación:** OpenAPI (Swagger)

## 🏛️ Arquitectura y Patrones de Diseño
Este proyecto está fundamentado en principios de ingeniería de software de alto nivel para garantizar escalabilidad, mantenibilidad y consistencia transaccional:
* **Arquitectura Hexagonal (Ports & Adapters):** Separación estricta entre el dominio financiero y la infraestructura tecnológica.
* **Domain-Driven Design (DDD):** Lógica de negocio encapsulada en entidades y casos de uso puros.
* **CQRS (Command Query Responsibility Segregation):** Separación de modelos de lectura (historial, saldos) y escritura (transferencias).
* **Bloqueo Optimista (Optimistic Locking):** Protección a nivel de base de datos contra el doble gasto en entornos de alta concurrencia.
* **Patrón Outbox:** Garantía de entrega de eventos de dominio mediante procesamiento asíncrono (`@Scheduled`).
* **Idempotencia:** Prevención de transacciones duplicadas mediante `Idempotency-Key`.

---

## 📖 Guía de Pruebas Rápidas (Swagger)

La API está documentada y lista para pruebas a través de Swagger UI. Al contar con un sistema de seguridad cerrado, es necesario autenticarse.

### 1. Obtener el Token (Pase VIP)
1. Ejecutar la prueba unitaria `GeneradorTokenTest.fabricarTokenVIP()` ubicada en `src/test/java/...`.
2. Copiar el token JWT generado en la consola (cadena que inicia con `eyJ...`).

### 2. Autenticación en Swagger
1. Levantar la aplicación con el perfil `dev` (`mvn spring-boot:run`).
2. Abrir el navegador en: `http://localhost:8080/swagger-ui.html`
3. Hacer clic en el botón verde **Authorize** (🔓).
4. Pegar el token JWT y hacer clic en "Authorize". El candado debe quedar cerrado (🔒).

### 3. Endpoints Principales a Probar
* **`GET /api/v1/billeteras/mi-saldo`**: Retorna el saldo exacto actual.
* **`GET /api/v1/billeteras/historial`**: Retorna la paginación del historial de movimientos (dejar el objeto `pageable` vacío antes de ejecutar).
* **`POST /api/v1/transferencias`**: Permite enviar fondos. 
  * *Requisito:* Proveer un UUID en el Header `Idempotency-Key`.
  * *Validaciones Activas:* Retornará `404` si la billetera destino no existe, y `422` si el saldo es insuficiente.

---

# E-commerce Order Saga (Event-Driven, Reactive)

## 1. Descripción general

Sistema de gestión de órdenes de e-commerce basado en:

- **Arquitectura limpia / hexagonal**
- **Event-Driven Architecture** con Kafka
- **Programación reactiva** con Spring WebFlux y Project Reactor
- **CQRS + Event Sourcing (parcial)** para órdenes

Microservicios:

- `order-service` (8080): core de órdenes + event sourcing.
- `payment-service` (8081): procesamiento de pagos.
- `notification-service` (8082): registro de notificaciones.

## 2. Decisiones técnicas principales

- **Stack**:
  - Java 17, Spring Boot 3.x, Spring WebFlux.
  - Apache Kafka, Postgres (R2DBC), MongoDB Reactive.
  - Docker + Docker Compose.

- **Patrones**:
  - Clean/Hexagonal: dominio independiente de frameworks.
  - CQRS: separación de comandos/queries.
  - Event Sourcing (órdenes → event store en Mongo).
  - Saga pattern coreografiada vía eventos en Kafka.
  - Idempotencia en consumidores (Order, Payment, Notification).

Trade-offs:
- Más complejidad inicial para ganar escalabilidad, testabilidad y extensibilidad.

## 3. Prerrequisitos

- Java 17+
- Maven 3.9+
- Docker + Docker Compose
- (Opcional) Postman para probar APIs

## 4. Instalación y ejecución

### 4.1. Variables de entorno

Copiar `.env.example` a `.env` y ajustar si es necesario (puertos, credenciales, etc.).

### 4.2. Ejecutar con Docker Compose

Desde la raíz del proyecto:

```bash
docker-compose up --build
```

O usando el script:

```bash
# Linux / Mac
./scripts/install.sh

# Windows
install.bat
```

Esto levanta:

- Postgres (`orders_postgres`)
- Mongo (`orders_mongo`)
- Kafka + Zookeeper
- order-service (8080)
- payment-service (8081)
- notification-service (8082)

## 5. Tests

Ejecutar todos los tests:

```bash
mvn test
```

Cobertura con JaCoCo (por servicio):

- `order-service/target/site/jacoco/index.html`
- `payment-service/target/site/jacoco/index.html`
- `notification-service/target/site/jacoco/index.html`

## 6. Endpoints disponibles

### Order Service (http://localhost:8080/api/v1)

- `POST /orders` – Crear orden
- `GET /orders/{id}` – Obtener orden por ID
- `GET /orders?customerId=X` – Listar órdenes de un cliente
- `PATCH /orders/{id}/cancel` – Cancelar orden
- `GET /orders/{id}/events` – Historial (event sourcing)

Swagger/OpenAPI:
- `http://localhost:8080/swagger-ui.html` (o ruta equivalente según config)

### Payment Service (http://localhost:8081/api/v1)

- `GET /payments/{orderId}` – Estado de pago
- `POST /payments/{orderId}/retry` – Reintentar pago

### Notification Service (http://localhost:8082/api/v1)

- `GET /notifications?orderId=X` – Notificaciones por orden

## 7. Documentación adicional

- `docs/ADRs/` – Architecture Decision Records
- `docs/architecture/` – Diagramas (componentes, eventos, estados, DB)
- `docs/api/` – OpenAPI + Postman collection

## 8. Mejoras futuras

- Gateway con Spring Cloud Gateway.
- Outbox Pattern para consistencia Order ↔ Kafka.
- Autenticación y autorización (OAuth2/JWT).
- Circuit breakers (Resilience4j).
- Métricas de negocio y SLOs formales.
- Manifests de Kubernetes + pipeline CI/CD.

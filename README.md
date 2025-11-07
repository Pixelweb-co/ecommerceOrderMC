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

### 4.1. Ejecutar con Docker Compose

Desde la raíz del proyecto:

```bash
docker-compose up --build
```

O usando elbo script:

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

### Payment Service (http://localhost:8081/api/v1)

- `GET /payments/{orderId}` – Estado de pago
- `POST /payments/{orderId}/retry` – Reintentar pago

### Notification Service (http://localhost:8082/api/v1)

- `GET /notifications?orderId=X` – Notificaciones por orden

## 7. Documentación de APIs (Swagger / OpenAPI)

Cada servicio expone su documentación OpenAPI con Swagger UI (usando springdoc-openapi):

- **Order Service**  
  - Swagger UI: `http://localhost:8080/swagger-ui.html` (o `http://localhost:8080/swagger-ui/index.html` según config)  
  - OpenAPI JSON/YAML: `http://localhost:8080/v3/api-docs`

- **Payment Service**  
  - Swagger UI: `http://localhost:8081/swagger-ui.html` (o `http://localhost:8081/swagger-ui/index.html`)  
  - OpenAPI JSON/YAML: `http://localhost:8081/v3/api-docs`

- **Notification Service**  
  - Swagger UI: `http://localhost:8082/swagger-ui.html` (o `http://localhost:8082/swagger-ui/index.html`)  
  - OpenAPI JSON/YAML: `http://localhost:8082/v3/api-docs`

Además, en `docs/api/` se incluyen:

- `order-service-openapi.yaml`
- `payment-service-openapi.yaml`
- `notification-service-openapi.yaml`
- `postman-collection.json`

## 8. Documentación adicional

- `docs/ADRs/` – Architecture Decision Records
- `docs/architecture/` – Diagramas (componentes, eventos, estados, DB)
- `docs/api/` – OpenAPI + Postman collection

## 9. Mejoras futuras

- Gateway con Spring Cloud Gateway.
- Outbox Pattern para consistencia Order ↔ Kafka.
- Autenticación y autorización (OAuth2/JWT).
- Circuit breakers (Resilience4j).
- Métricas de negocio y SLOs formales.
- Manifests de Kubernetes + pipeline CI/CD.

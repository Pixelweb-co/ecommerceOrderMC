# Autoevaluación

## 1. Funcionalidades completadas

- Order Service:
  - Crear orden (PENDING → CONFIRMED con validación de inventario simulada).
  - Consultar orden por ID.
  - Listar órdenes por cliente con paginación.
  - Cancelar orden en estados PENDING/CONFIRMED.
  - Historial de eventos (event sourcing sobre Mongo).
  - Publicación de eventos `ORDER_CREATED`, `ORDER_CONFIRMED`, `ORDER_STATUS_CHANGED`.

- Payment Service:
  - Consumo de `ORDER_CONFIRMED` desde Kafka.
  - Procesamiento de pago simulado.
  - Idempotencia por `orderId`.
  - Emisión de `PAYMENT_PROCESSED` / `PAYMENT_FAILED`.
  - `GET /payments/{orderId}` y `POST /payments/{orderId}/retry`.

- Notification Service:
  - Consumo de eventos de órdenes/pagos.
  - Registro de notificaciones en Mongo.
  - `GET /notifications?orderId=X`.
  - Idempotencia básica (no duplicar notificaciones iguales por orden).

- Testing:
  - Tests unitarios con JUnit 5 + Mockito + Reactor Test.
  - Tests de servicios de dominio y aplicación.

## 2. Funcionalidades pendientes

- [ ] Kafka Streams para procesamiento complejo de eventos.
- [ ] Outbox Pattern formal.
- [ ] Spring Cloud Gateway como API Gateway central.
- [ ] Seguridad con Spring Security (OAuth2/JWT).
- [ ] Pipeline CI/CD (GitHub Actions / GitLab CI).
- [ ] Manifests de Kubernetes.

Razones:
- Priorización de flujo core de negocio (Saga) y requisitos obligatorios.
- Tiempo limitado para implementar y documentar features avanzadas.

## 3. Decisiones con más tiempo

- Refinar la separación CQRS con modelos de lectura optimizados.
- Introducir un Gateway para centralizar cross-cutting concerns.
- Formalizar los SLIs/SLOs de negocio (tiempo medio de confirmación, tasa de fallos de pago, etc.).
- Implementar Outbox para consistencia fuerte entre DB transaccional y Kafka.

## 4. Desafíos enfrentados

- Modelar la Saga end-to-end en modo reactivo (WebFlux + Kafka).
- Asegurar idempotencia en consumidores de eventos.
- Manejo de estados de la orden y transiciones (máquina de estados).

Cómo se resolvieron:
- Uso de estados explícitos y eventos de dominio.
- Validaciones de status antes de aplicar cambios (ej. ignorar pagos si ya está PAID/FAILED).
- Tests unitarios con StepVerifier para validar flujos reactivos.

## 5. Trade-offs realizados

- Simplificación de validación de inventario (reglas simuladas) en lugar de un Inventory Service real.
- Manejo de seguridad mínimo (sin OAuth2/JWT) para enfocarse en la arquitectura de eventos.
- Observabilidad implementada a nivel básico (logs estructurados, correlation ID esbozado),
  dejando métricas avanzadas para futuro.

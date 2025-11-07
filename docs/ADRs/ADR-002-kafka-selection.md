# ADR-002: Elección de Kafka como message broker

## Estado
Aceptado

## Contexto
El flujo de órdenes requiere procesamiento asíncrono y event-driven:

- Picos de tráfico (promociones).
- Integración entre múltiples microservicios (order, payment, notification).
- Necesidad de reintentos, idempotencia y re-procesamiento de eventos.

## Decisión
Se utiliza **Apache Kafka** como message broker principal:

- Topic `order-events` para eventos de dominio de órdenes.
- Topic `payment-events` para eventos de pago.
- Cada servicio tiene su propio consumer group para escalabilidad horizontal.

## Consecuencias

Positivas:
- Alta capacidad de throughput y retención de eventos.
- Reproducción de eventos para debugging y auditoría.
- Integración natural con patrones de Event Sourcing y Outbox.

Negativas:
- Mayor complejidad operativa que una cola simple (ej. RabbitMQ, SQS).
- Necesidad de administrar particiones, offsets, retención, etc.

## Alternativas Consideradas

1. **RabbitMQ**  
   Rechazada: muy buena para colas de trabajo, pero Kafka se alinea mejor con
   event sourcing y replay de eventos.

2. **Mensajería en base de datos (polling)**  
   Rechazada: acoplamiento con el datastore transaccional, peor escalabilidad
   y mayor complejidad para reintentos y backpressure.

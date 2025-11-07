# ADR-003: Manejo de errores y compensaciones en la Saga de órdenes

## Estado
Aceptado

## Contexto
El flujo distribuido de la orden es:

OrderCreated → ValidateInventory → OrderConfirmed → ProcessPayment →
PaymentProcessed/PaymentFailed → Notificar + finalizar orden.

Se necesitan:
- Idempotencia.
- Manejo consistente de errores.
- Posible compensación (ej. cancelar orden cuando el pago falla).

## Decisión

- **Saga coreografiada por eventos**:
  - `order-events` y `payment-events` coordinan el flujo.
  - Order Service reacciona a `PAYMENT_PROCESSED` / `PAYMENT_FAILED`.
- **Estrategia de errores**:
  - Si inventario falla → `ORDER_FAILED`.
  - Si pago falla → `PAYMENT_FAILED` + `ORDER_STATUS_CHANGED` a `FAILED`.
- **Idempotencia**:
  - Order Service ignora eventos de pago si la orden ya está en `PAID` o `FAILED`.
  - Payment Service no reprocesa pagos con status final `SUCCESS`.
  - Notification Service evita duplicar notificaciones por `orderId + type + message`.

## Consecuencias

Positivas:
- Flujo desacoplado; cada servicio reacciona a eventos de negocio.
- Manejo explícito de estados de error en la orden.
- Facilita extender la Saga (ej. SHIPPED, DELIVERED) sin romper todo.

Negativas:
- Debugging más complejo (trazabilidad distribuida).
- Necesidad de buenas métricas y logs correlacionados.

## Alternativas Consideradas

1. **Saga orquestada con un orquestador central**  
   Rechazada por introducir un único punto de fallo y más acoplamiento.

2. **Transacciones distribuidas 2PC**  
   Rechazada por complejidad, poca alineación con microservicios y baja escalabilidad.

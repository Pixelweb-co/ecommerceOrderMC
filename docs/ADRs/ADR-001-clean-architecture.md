# ADR-001: Uso de Clean Architecture / Hexagonal

## Estado
Aceptado

## Contexto
El sistema de e-commerce debe ser mantenible, testeable y fácil de extender
(más servicios, nuevos canales, nuevos métodos de pago). También debe permitir
cambiar frameworks (Web, persistencia, mensajería) con impacto mínimo en el dominio.

## Decisión
Se adopta Clean Architecture / Hexagonal con:

- **Capa de dominio** (módulo `domain` dentro de cada servicio):
  - Entidades (`Order`, `Payment`, `Notification`, etc.).
  - Value Objects (IDs, montos, estados, etc.).
  - Puertos (interfaces) de repositorios, event store y publishers.
  - Eventos de dominio (`OrderEvent`, `PaymentEvent`, etc.).

- **Capa de aplicación**:
  - Servicios de caso de uso (`OrderCommandService`, `OrderQueryService`, etc.).
  - Orquestación de la Saga (en Order/Payment vía eventos).

- **Adaptadores de infraestructura**:
  - REST (Spring WebFlux controllers).
  - Persistencia (R2DBC Postgres, Mongo reactive).
  - Mensajería (Kafka producers/consumers).

Las dependencias siempre apuntan hacia el dominio:
Infraestructura → Aplicación → Dominio.

## Consecuencias

Positivas:
- Alta testabilidad del dominio (sin depender de Spring).
- Facilidad para cambiar R2DBC, Kafka, Mongo, etc. con mínimo impacto.
- Código más modular y alineado con DDD.

Negativas:
- Más clases y capas (mayor complejidad inicial).
- Curva de aprendizaje para nuevos miembros del equipo.

## Alternativas Consideradas

1. **Arquitectura en capas clásica (Controller → Service → Repository)**  
   Rechazada porque acopla fuertemente dominio con infraestructura y dificulta
   la introducción de CQRS, event sourcing y mensajería.

2. **Monolito sin separación explícita de dominio**  
   Rechazada por problemas de mantenibilidad y escalabilidad a mediano plazo.

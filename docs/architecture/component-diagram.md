```mermaid
flowchart LR
    subgraph Client
        Postman[Postman / Frontend\n(REST client)]
    end

    subgraph OrderService[Order Service]
        OS_API[WebFlux REST API]
        OS_App[Application Layer\n(Command/Query Services)]
        OS_Domain[Domain Layer\n(Order, OrderEvent, Ports)]
        OS_Repo[(Postgres R2DBC)]
        OS_EventStore[(Mongo Event Store)]
    end

    subgraph PaymentService[Payment Service]
        PS_API[WebFlux REST API]
        PS_App[Application Layer]
        PS_Domain[Domain Layer\n(Payment, PaymentEvent)]
        PS_Repo[(Postgres R2DBC)]
    end

    subgraph NotificationService[Notification Service]
        NS_API[WebFlux REST API]
        NS_App[Application Layer]
        NS_Domain[Domain Layer\n(Notification)]
        NS_Repo[(Mongo Reactive)]
    end

    subgraph Infra[Infra]
        Kafka[(Kafka)]
        ZK[(Zookeeper)]
    end

    Postman --> OS_API
    Postman --> PS_API
    Postman --> NS_API

    OS_API --> OS_App --> OS_Domain
    OS_Domain --> OS_Repo
    OS_Domain --> OS_EventStore

    PS_API --> PS_App --> PS_Domain
    PS_Domain --> PS_Repo

    NS_API --> NS_App --> NS_Domain
    NS_Domain --> NS_Repo

    OS_Domain -- publish --> Kafka
    Kafka -- order-events --> PS_App
    Kafka -- payment-events --> OS_App
    Kafka -- order-events/payment-events --> NS_App
```
-- Extensión para UUID (por si la necesitas)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =======================
-- Tabla de órdenes
-- =======================
CREATE TABLE IF NOT EXISTS orders (
    id           UUID PRIMARY KEY,
    customer_id  VARCHAR(64)      NOT NULL,
    total_amount NUMERIC(19, 2)   NOT NULL,
    status       VARCHAR(32)      NOT NULL,
    created_at   TIMESTAMPTZ      NOT NULL,
    updated_at   TIMESTAMPTZ      NOT NULL
);

-- =======================
-- Tabla de pagos
-- =======================
CREATE TABLE IF NOT EXISTS payments (
    id             UUID PRIMARY KEY,
    order_id       UUID            NOT NULL,
    amount         NUMERIC(19, 2)  NOT NULL,
    status         VARCHAR(32)     NOT NULL,
    failure_reason TEXT,
    created_at     TIMESTAMPTZ     NOT NULL,
    updated_at     TIMESTAMPTZ     NOT NULL
);


-- =======================
-- Relaciones y constraints
-- =======================

-- FK payments → orders
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
            REFERENCES orders (id);

-- =======================
-- Índices para performance
-- =======================

-- Búsqueda por cliente
CREATE INDEX IF NOT EXISTS idx_orders_customer_id
    ON orders (customer_id);

-- Búsqueda por estado de orden
CREATE INDEX IF NOT EXISTS idx_orders_status
    ON orders (status);

-- Búsqueda de pagos por orden
CREATE INDEX IF NOT EXISTS idx_payments_order_id
    ON payments (order_id);



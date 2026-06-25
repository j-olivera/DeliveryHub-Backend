CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE orders (
    id                UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_name     VARCHAR(100) NOT NULL,
    customer_phone    VARCHAR(20),
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                          CHECK (status IN ('PENDING','ASSIGNED','IN_PROGRESS','DELIVERED','CANCELLED')),
    admin_user_id     UUID         NOT NULL REFERENCES users(id),
    delivery_user_id  UUID         REFERENCES users(id),  -- nullable hasta asignación
    notes             TEXT,
    latitude          DECIMAL(10,7),
    longitude         DECIMAL(10,7),
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    assigned_at       TIMESTAMP,
    delivered_at      TIMESTAMP
);

CREATE INDEX idx_orders_status           ON orders(status);
CREATE INDEX idx_orders_delivery_user_id ON orders(delivery_user_id);
CREATE INDEX idx_orders_created_at       ON orders(created_at DESC);

CREATE TABLE addresses (
    id        UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id  UUID         NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    street    VARCHAR(200) NOT NULL,
    city      VARCHAR(100) NOT NULL,
    zip_code  VARCHAR(20),
    latitude  DECIMAL(10,7),
    longitude DECIMAL(10,7)
);

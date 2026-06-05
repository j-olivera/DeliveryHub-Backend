# Base de datos — DeliveryHub

## Motor
PostgreSQL 16 con extensión `uuid-ossp` habilitada. PostGIS instalado y listo para activar cuando se implemente tracking GPS.

## Migraciones
Flyway. Todos los cambios de schema van en `src/main/resources/db/migration/`.

Nombrado de archivos: `V{número}__{descripción_en_snake_case}.sql`
```
V1__create_users_table.sql
V2__create_orders_table.sql
V3__add_coordinates_to_orders.sql
```

Reglas:
- Nunca modificar una migración ya ejecutada en cualquier entorno.
- Cada migración es atómica: si falla, no deja el schema en estado intermedio.
- Los índices van en la misma migración que crea la tabla.

## Schema

### users
```sql
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,  -- bcrypt hash
    role        VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'DELIVERY')),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role  ON users(role);
```

### orders
```sql
CREATE TABLE orders (
    id                UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_name     VARCHAR(100) NOT NULL,
    customer_phone    VARCHAR(20),
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                          CHECK (status IN ('PENDING','ASSIGNED','IN_PROGRESS','DELIVERED','CANCELLED')),
    admin_user_id     UUID         NOT NULL REFERENCES users(id),
    delivery_user_id  UUID         REFERENCES users(id),  -- nullable hasta asignación
    notes             TEXT,
    -- PostGIS-ready: coordenadas almacenadas desde el MVP
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
```

### addresses
```sql
CREATE TABLE addresses (
    id        UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id  UUID         NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    street    VARCHAR(200) NOT NULL,
    city      VARCHAR(100) NOT NULL,
    zip_code  VARCHAR(20),
    latitude  DECIMAL(10,7),
    longitude DECIMAL(10,7)
);
```

## Convenciones de schema

- Nombres de tablas: `snake_case` en plural.
- Nombres de columnas: `snake_case`.
- Todas las tablas tienen `id UUID`, `created_at` y `updated_at`.
- Claves foráneas siempre con nombre explícito de constraint.
- Nunca usar `SERIAL` o `BIGSERIAL` — siempre `UUID`.
- Los enums de dominio se almacenan como `VARCHAR` con `CHECK constraint`, no como tipos ENUM de Postgres (más fácil de migrar).

## Conexión local (Docker)

```
Host:     localhost
Puerto:   5432
Base:     deliveryhub_db
Usuario:  deliveryhub_user
Password: deliveryhub_pass
```

pgAdmin disponible en `http://localhost:5050`
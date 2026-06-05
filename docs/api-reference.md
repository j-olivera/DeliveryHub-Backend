# API Reference — DeliveryHub

Base URL: `/api/v1`
Autenticación: `Authorization: Bearer <jwt_token>` en todos los endpoints excepto `/auth/**`

## Convenciones generales

- Todos los IDs son `UUID` en formato string.
- Fechas en formato ISO 8601: `2024-01-15T10:30:00Z`
- Respuestas de error siempre incluyen `{ "message": "...", "timestamp": "..." }`
- Paginación con `?page=0&size=20&sort=createdAt,desc`

---

## Auth

### POST /auth/login
Autentica un usuario y retorna el JWT.

**Request**
```json
{
  "email": "admin@deliveryhub.com",
  "password": "password123"
}
```

**Response 200**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "ADMIN",
  "userId": "a1b2c3d4-..."
}
```

---

## Orders

### GET /orders
Retorna pedidos. ADMIN ve todos; DELIVERY ve solo los suyos (filtro automático en backend).

**Roles:** ADMIN, DELIVERY

**Query params:** `status`, `page`, `size`

**Response 200**
```json
{
  "content": [
    {
      "id": "uuid",
      "customerName": "Juan García",
      "status": "PENDING",
      "address": {
        "street": "Av. Rivadavia 1234",
        "city": "Buenos Aires",
        "coordinates": { "lat": -34.6037, "lng": -58.3816 }
      },
      "deliveryUserId": null,
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "totalElements": 42,
  "totalPages": 3
}
```

---

### POST /orders
Crea un nuevo pedido en estado PENDING.

**Roles:** ADMIN

**Request**
```json
{
  "customerName": "Juan García",
  "customerPhone": "+5491112345678",
  "address": {
    "street": "Av. Rivadavia 1234",
    "city": "Buenos Aires",
    "coordinates": { "lat": -34.6037, "lng": -58.3816 }
  },
  "notes": "Departamento 4B, timbre roto"
}
```

**Response 201**
```json
{
  "id": "uuid",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

### PATCH /orders/{id}/assign
Asigna un pedido a un repartidor. El pedido debe estar en PENDING.

**Roles:** ADMIN

**Request**
```json
{
  "deliveryUserId": "uuid-del-repartidor"
}
```

**Response 200**
```json
{
  "id": "uuid",
  "status": "ASSIGNED",
  "deliveryUserId": "uuid-del-repartidor"
}
```

**Errores:**
- `404` — Pedido no encontrado
- `422` — El pedido no está en estado PENDING

---

### PATCH /orders/{id}/status
Actualiza el estado del pedido. Solo las transiciones válidas según el rol del usuario.

**Roles:** DELIVERY (IN_PROGRESS, DELIVERED) / ADMIN (CANCELLED)

**Request**
```json
{
  "status": "IN_PROGRESS"
}
```

**Response 200**
```json
{
  "id": "uuid",
  "status": "IN_PROGRESS",
  "updatedAt": "2024-01-15T11:00:00Z"
}
```

**Errores:**
- `403` — El delivery no es el asignado al pedido
- `422` — Transición de estado inválida

---

### DELETE /orders/{id}
Cancela un pedido (equivale a cambiar estado a CANCELLED).

**Roles:** ADMIN

**Response 204** — Sin cuerpo

---

## Users (Repartidores)

### GET /users
Lista todos los repartidores activos.

**Roles:** ADMIN

**Response 200**
```json
[
  {
    "id": "uuid",
    "name": "Carlos Rodríguez",
    "email": "carlos@deliveryhub.com",
    "role": "DELIVERY",
    "active": true
  }
]
```

### POST /users
Crea un nuevo usuario.

**Roles:** ADMIN

**Request**
```json
{
  "name": "Carlos Rodríguez",
  "email": "carlos@deliveryhub.com",
  "password": "temporal123",
  "role": "DELIVERY"
}
```

**Response 201**
```json
{
  "id": "uuid",
  "name": "Carlos Rodríguez",
  "role": "DELIVERY"
}
```

---

## Batch assignment

### POST /orders/batch-assign
Asigna múltiples pedidos a un repartidor en una sola operación.

**Roles:** ADMIN

**Request**
```json
{
  "orderIds": ["uuid-1", "uuid-2", "uuid-3"],
  "deliveryUserId": "uuid-del-repartidor"
}
```

**Response 200**
```json
{
  "assigned": ["uuid-1", "uuid-2"],
  "failed": [
    { "orderId": "uuid-3", "reason": "El pedido no está en estado PENDING" }
  ]
}
```
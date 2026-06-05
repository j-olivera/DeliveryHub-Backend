# Arquitectura — DeliveryHub

## Clean Architecture: regla de dependencias

Las dependencias siempre apuntan hacia el centro. Las capas internas no saben nada de las externas.

```
┌─────────────────────────────────────┐
│           Presentation              │  Controllers REST, WebSocket
│  ┌──────────────────────────────┐   │
│  │        Infrastructure        │   │  JPA, Security, WebSocket config
│  │  ┌───────────────────────┐   │   │
│  │  │      Application      │   │   │  Use Cases, Ports (interfaces)
│  │  │  ┌────────────────┐   │   │   │
│  │  │  │     Domain     │   │   │   │  Entidades, Enums, Excepciones
│  │  │  └────────────────┘   │   │   │
│  │  └───────────────────────┘   │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
```

## Capas y responsabilidades

### Domain
- Entidades Java puras: `Order`, `User`, `Address`
- Enums: `OrderStatus`, `UserRole`
- Excepciones de dominio: `OrderNotFoundException`, `InvalidStatusTransitionException`
- **Sin ninguna anotación de Spring, JPA o Jackson**

### Application
- Un archivo por caso de uso: `AssignOrderUseCase`, `UpdateOrderStatusUseCase`
- Ports de entrada (`port/in`): interfaces que expone el caso de uso al mundo exterior
- Ports de salida (`port/out`): interfaces que el caso de uso necesita de infraestructura
- DTOs internos para comunicación entre capas

### Infrastructure
- Implementa los ports de salida definidos en Application
- `persistence/`: entidades JPA (`OrderEntity`), repositorios Spring Data
- `security/`: `JwtFilter`, `SecurityConfig`, `UserDetailsServiceImpl`
- `websocket/`: `WebSocketConfig`, `StompEventListener`
- `mapper/`: MapStruct mappers entre Entity ↔ Domain ↔ DTO

### Presentation
- Controllers REST: uno por entidad (`OrderController`, `UserController`)
- Controllers WebSocket: `@MessageMapping`
- DTOs HTTP: `CreateOrderRequest`, `OrderResponse`, `AssignOrderRequest`
- `@RestControllerAdvice` global para manejo de errores

## Decisiones de diseño clave

### Por qué JWT stateless
El backend no guarda estado de sesión. Cada request es autónomo. Esto permite escalar horizontalmente agregando instancias sin necesidad de sesiones compartidas.

### Por qué Leaflet sobre Google Maps
La API de Google Maps requiere tarjeta de crédito incluso en el tier gratuito. Leaflet + OpenStreetMap es completamente gratuito, open-source, y suficiente para el MVP. La integración con el cliente de mapas está aislada en Infrastructure, lo que permite migrar a Google Maps en el futuro sin tocar Application ni Domain.

### Por qué STOMP sobre WebSocket puro
STOMP agrega un protocolo de mensajería sobre WebSocket: topics, subscripciones, headers. Simplifica la lógica de broadcasting y permite integrarlo limpiamente con Spring Security para validar el token JWT en el handshake.

### Por qué PostGIS-ready desde el inicio
El schema de PostgreSQL incluye columnas de tipo `POINT` para coordenadas desde la primera migración, aunque no se usen en el MVP. Esto evita una migración disruptiva cuando se implemente el tracking GPS.

## Flujo de una request HTTP típica

```
HTTP Request
    ↓
JwtFilter (valida token, setea SecurityContext)
    ↓
OrderController (deserializa JSON → CreateOrderRequest DTO)
    ↓
CreateOrderUseCase (valida reglas, construye entidad Order)
    ↓
OrderRepositoryPort (interface)
    ↓
OrderJpaRepository (implementación concreta, guarda en PostgreSQL)
    ↓
WebSocket broadcast (notifica a clientes suscritos)
    ↓
HTTP Response (OrderResponse DTO)
```
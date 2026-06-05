# DeliveryHub — AGENTS.md

Sistema de gestión logística interna para negocios de comida.
Optimiza el flujo Administrador → Repartidor con trazabilidad en tiempo real.

---

## Stack

| Capa         | Tecnología                              |
|--------------|-----------------------------------------|
| Backend      | Java 21, Spring Boot 3, Spring Security |
| Frontend     | Angular 21, Signals, RxJS               |
| Base de datos| PostgreSQL (PostGIS-ready)              |
| Tiempo real  | WebSockets (STOMP over SockJS)          |
| Infra        | Docker Compose, Flyway                  |

---

## 1. Arquitectura

El proyecto aplica **Clean Architecture**. La regla fundamental es que las dependencias siempre apuntan hacia adentro: ninguna capa interna conoce las capas externas.

```
Presentation → Application → Domain
Infrastructure → Application → Domain
```

- **Domain** — Entidades puras Java. Sin Spring, sin JPA, sin JSON.
- **Application** — Casos de uso. Orquestan el dominio e interactúan con el exterior solo a través de interfaces (ports).
- **Infrastructure** — Implementaciones concretas: repositorios JPA, configuración WebSocket, clientes HTTP externos.
- **Presentation** — Controllers REST y WebSocket. Reciben DTOs, delegan al caso de uso correspondiente.

> Antes de crear cualquier clase, identificar a qué capa pertenece y ubicarla en el paquete correcto.
> Nunca resolver un problema de infraestructura modificando el Domain.

---

## 2. Estructura del proyecto

```
src/
└── main/
    └── java/com/deliveryhub/
        ├── domain/
        │   ├── model/          # Entidades: Order, User, Address
        │   ├── enums/          # OrderStatus, UserRole
        │   └── exception/      # Excepciones de dominio puras
        ├── application/
        │   ├── mapper/         # Mappers de entidades a DTOs y viceversa
        │   ├── usecase/        # Un archivo por caso de uso
        │   ├── port/
        │   │   ├── in/         # Interfaces de entrada (lo que expone el use case)
        │   │   └── out/        # Interfaces de salida (repositorios, servicios externos)
        │   └── dto/            # DTOs internos entre capas
        ├── infrastructure/
        │   ├── persistence/    # Implementaciones JPA de los ports out
        │   ├── websocket/      # Configuración y handlers STOMP
        │   ├── security/       # JWT filter, config de Spring Security
        │   └── mapper/         # MapStruct mappers (Entity ↔ Domain ↔ DTO)
        └── presentation/
            ├── rest/           # @RestController — un controller por entidad
            ├── websocket/      # @MessageMapping controllers
            └── dto/            # Request y Response DTOs (los que viajan por HTTP)
```

---

## 3. Convenciones de código

- **Idioma del código**: inglés — clases, métodos, variables, DTOs, enums, nombres de archivos.
- **Idioma de comentarios y Javadoc**: español.
- **Nomenclatura**:
  - `camelCase` → variables y métodos
  - `PascalCase` → clases e interfaces
  - `UPPER_SNAKE_CASE` → constantes y valores de enum
- **Sin abreviaciones**: `orderRepository` no `ordRepo`, `deliveryUserId` no `dUsrId`.
- **Un concepto, un archivo**: no agrupar múltiples clases en un mismo `.java`.
- **Inmutabilidad por defecto**: preferir `final` en campos, evitar setters innecesarios.
- Los DTOs de request terminan en `Request`, los de response en `Response`.
  - Ejemplo: `CreateOrderRequest`, `OrderStatusResponse`
- PROHIBIDO usar emojis en el codigo
---

## 4. Convenciones de comentarios

- Comentar el **por qué**, nunca el qué (el código debe ser autoexplicativo).
- Javadoc en español en todos los métodos públicos de casos de uso y ports.
- No dejar comentarios de código muerto (`// TODO` está permitido si describe una tarea concreta).

```java
// Correcto: explica la decisión de negocio
// Solo el delivery asignado puede iniciar el pedido, no otro repartidor
if (!order.getDeliveryUserId().equals(currentUserId)) {
    throw new UnauthorizedTransitionException(order.getId());
}

// Incorrecto: describe lo que el código ya dice
// Verificar si el id del usuario es igual al del delivery
```

---

## 5. Manejo de errores y excepciones

- Las excepciones de dominio van en `domain/exception/` y extienden `RuntimeException`.
- Las excepciones de dominio **no** importan nada de Spring.
- Nombrado: `[Concepto][Motivo]Exception` → `OrderNotFoundException`, `InvalidStatusTransitionException`, `UnauthorizedTransitionException`.
- Un único `@RestControllerAdvice` en la capa Presentation centraliza todos los handlers.
- Nunca retornar `null` como señal de error: usar excepciones o `Optional`.
- Los endpoints siempre retornan códigos HTTP semánticamente correctos:

| Situación                        | HTTP       |
|----------------------------------|------------|
| Recurso no encontrado            | 404        |
| Transición de estado inválida    | 422        |
| Sin permisos para la operación   | 403        |
| Error de validación de request   | 400        |
| Error inesperado del servidor    | 500        |

---

## 6. Testing y TDD

### Restricciones TDD
- Escribir el test **antes** que el código de producción. Sin excepciones.
- El ciclo es siempre: Red → Green → Refactor.
- No se hace commit de código de producción sin su test correspondiente.

### Qué testear y cómo

| Capa           | Qué testear                                               | Herramienta              |
|----------------|-----------------------------------------------------------|--------------------------|
| Domain         | Lógica de entidades, validaciones, transiciones de estado | JUnit 5 puro             |
| Application    | Casos de uso: flujo feliz + todos los casos de error      | JUnit 5 + Mockito        |
| Infrastructure | Repositorios contra base de datos real                    | @DataJpaTest + Testcontainers |
| Presentation   | Endpoints HTTP: request, response, códigos de estado      | @WebMvcTest + MockMvc    |
| Integración    | Flujo completo de un caso de uso crítico                  | @SpringBootTest           |

- Nombrado de tests: `[método]_[escenario]_[resultadoEsperado]`
  - Ejemplo: `assignOrder_whenOrderIsNotPending_shouldThrowException`
- Cada test verifica **una sola cosa**. Si necesita más de un assert, probablemente son dos tests.
- Los mocks solo se usan para dependencias externas (repositorios, APIs). Nunca mockear el dominio.

---

## 7. Git

### Branches
```
main          ← producción, siempre estable
develop       ← integración, base para features
feature/      ← una feature por branch
fix/          ← corrección de bugs
```

Nombrado de branches: `feature/assign-order-to-delivery`, `fix/jwt-token-expiration`

### Commits
Formato: `tipo(scope): descripción en español en imperativo`

| Tipo       | Cuándo usarlo                              |
|------------|--------------------------------------------|
| `feat`     | Nueva funcionalidad                        |
| `fix`      | Corrección de bug                          |
| `test`     | Agregar o corregir tests                   |
| `refactor` | Cambio sin agregar funcionalidad ni fix    |
| `docs`     | Documentación únicamente                   |
| `chore`    | Configuración, dependencias, build         |

Ejemplos:
```
feat(orders): agregar validación de transición de estado
fix(auth): corregir expiración del token JWT
test(orders): agregar tests para asignación de pedidos
refactor(use-case): extraer validación de ownership a método privado
```

- Un commit = un cambio lógico. No mezclar refactor con features.
- Los tests van en el mismo commit que el código que testean.

---

## 8. Protocolo de trabajo del agente

Antes de comenzar cualquier tarea:

1. Identificar a qué capa pertenece el cambio.
2. Leer los archivos existentes de esa capa antes de crear nuevos.
3. Consultar el doc de referencia correspondiente (ver sección siguiente).
4. Escribir el test primero (TDD).
5. No modificar más de lo necesario — cambios acotados al scope de la tarea.

Está **prohibido**:
- Saltar capas (llamar infraestructura desde el dominio).
- Crear lógica de negocio en controllers.
- Retornar entidades de dominio directamente desde endpoints (siempre DTOs).
- Hacer commits con tests en rojo.

---

## 9. Documentación de referencia

Consultar según el contexto de la tarea:

| Tarea                                        | Archivo                          |
|----------------------------------------------|----------------------------------|
| Crear o modificar un endpoint REST           | `docs/api-reference.md`          |
| Crear una clase nueva (cualquier capa)       | `docs/class-conventions.md`      |
| Implementar lógica de negocio o validaciones | `docs/domain-rules.md`           |
| Decisiones de arquitectura o diseño          | `docs/architecture.md`           |
| Crear o modificar tablas / migraciones       | `docs/database.md`               |
| Implementar o consumir WebSockets            | `docs/websocket.md`              |
| Configurar Docker o CI/CD                    | `docs/infrastructure.md`         |
| Escribir o corregir tests                    | `docs/testing.md`                |
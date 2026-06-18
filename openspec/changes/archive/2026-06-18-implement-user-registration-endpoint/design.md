# Design: User Registration REST Endpoint

Technical design for exposing the user registration flow via `POST /api/v1/users` with input validation and exception mapping.

## Technical Approach

We will implement a REST controller that delegates registration requests to the application layer's `RegisterUserUseCase`. The controller handles HTTP payload mapping and triggers JSR-380 input validation. A global exception handler maps domain and system exceptions to HTTP status codes (201, 400, 409, 500).

## Architecture Decisions

| Option | Tradeoff | Decision |
|---|---|---|
| Use JSR-380 validation (`@Valid` + annotations) | Requires adding `spring-boot-starter-validation`, but standard, declarative, and keeps controllers lean. | Choose JSR-380 via `spring-boot-starter-validation`. |
| Programmatic validation in controller | Avoids new dependency, but creates boilerplates and duplicates validation rules. | Reject. |

| Option | Tradeoff | Decision |
|---|---|---|
| Reuse Application DTO | Less classes, but couples presentation constraints (HTTP validation annotations) to application layer. | Use separate presentation-level DTOs mapping to application-level DTOs. |
| Dedicated Presentation DTO | Clean separation of concerns, complies with Clean Architecture. | Choose dedicated presentation DTOs. |

## Data Flow

```text
HTTP POST /api/v1/users ──> UserController ──(validates @Valid)──> maps DTO ──> RegisterUserUseCase ──> Domain/Database
                                │                                                    │
                   (Throws validation/domain exception)                 (Throws UserAlreadyExistsException)
                                │                                                    │
                                └───────> GlobalExceptionHandler ────────────────────┘
                                                     │
                                           JSON Error Response
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `pom.xml` | Modify | Add `spring-boot-starter-validation` dependency. |
| `src/main/java/com/deliveryhub/presentation/dto/RegisterUserRequest.java` | Create | Presentation request DTO with JSR-380 validation annotations. |
| `src/main/java/com/deliveryhub/presentation/dto/UserResponse.java` | Create | Response DTO containing `id`, `name`, `email`, and `role`. |
| `src/main/java/com/deliveryhub/presentation/dto/ErrorResponse.java` | Create | Standardized error payload with message and timestamp. |
| `src/main/java/com/deliveryhub/presentation/controller/UserController.java` | Create | REST controller mapping `POST /api/v1/users` to `RegisterUserUseCase`. |
| `src/main/java/com/deliveryhub/presentation/controller/GlobalExceptionHandler.java` | Create | Global controller advice for mapping exceptions to HTTP status codes. |
| `src/test/java/com/deliveryhub/presentation/controller/UserControllerTest.java` | Create | Unit tests validating success, validation failures, and duplicates. |

## Interfaces / Contracts

### POST /api/v1/users Request Payload
```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "password": "pwd123",
  "role": "DELIVERY"
}
```

### POST /api/v1/users Response (201 Created)
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Alice",
  "email": "alice@example.com",
  "role": "DELIVERY"
}
```

### Error Response (400 Bad Request / 409 Conflict)
```json
{
  "message": "Error details",
  "timestamp": "2026-06-18T20:20:21.123456"
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit (Presentation) | Success registration (201) | Mock `RegisterUserUseCase`, verify fields in returned JSON |
| Unit (Presentation) | Blank fields / invalid email format / invalid role (400) | Perform POST with invalid payloads, verify 400 and error message |
| Unit (Presentation) | Duplicate user registration (409) | Mock `RegisterUserUseCase` to throw `UserAlreadyExistsException`, verify 409 |

## Migration / Rollout

No database migrations or feature flags required. Simple rollout with the rest of the application.

## Open Questions

None.

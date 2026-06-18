# Design: Implement User Application Layer

## Technical Approach

Define the application layer contracts and implementation for user registration under `com.deliveryhub.application`. The registration flow enforces input format validation via the `User` domain entity, uniqueness checks for email/username via the repository output port, and user persistence.

## Architecture Decisions

### Decision: User Validation & Uniqueness Flow Sequence

| Option | Tradeoff | Decision |
|---|---|---|
| **Domain Instantiation First** | Validates structural fields (not blank, email regex) via `User.create()` before querying DB. Saves DB resources on invalid requests. | **Chosen** |
| **Uniqueness Check First** | Performs DB queries first, executing queries even if parameters are blank or malformed. | Rejected |

### Decision: UserRole Constraint Handling

| Option | Tradeoff | Decision |
|---|---|---|
| **Enforce Existing Roles Only** | Keeps `UserRole` enum as `ADMIN` and `DELIVERY`. Avoids breaking existing unit tests/behavior. | **Chosen** |
| **Introduce CUSTOMER Role** | Matches a scenario placeholder in spec but requires modifying core enum and domain test code beyond current scope. | Rejected |

## Data Flow

```
RegisterUserRequest ──→ RegisterUserUseCaseImpl
                             │
                             ├─→ User.create() (Validate email/fields)
                             │
                             ├─→ UserRepositoryPort.existsByEmail/Name() (Unique check)
                             │
                             └─→ UserRepositoryPort.save() ──→ Registered User
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/deliveryhub/domain/exception/UserAlreadyExistsException.java` | Create | Exception thrown when email or name is already taken. |
| `src/main/java/com/deliveryhub/application/dto/RegisterUserRequest.java` | Create | DTO record containing registration details (name, email, password, role). |
| `src/main/java/com/deliveryhub/application/port/in/RegisterUserUseCase.java` | Create | Input port interface exposing registration method. |
| `src/main/java/com/deliveryhub/application/port/out/UserRepositoryPort.java` | Create | Output port interface for User persistence. |
| `src/main/java/com/deliveryhub/application/usecase/RegisterUserUseCaseImpl.java` | Create | Use case implementation enforcing validation and delegation. |
| `src/test/java/com/deliveryhub/application/usecase/RegisterUserUseCaseTest.java` | Create | JUnit 5 unit tests for registration business logic. |

## Interfaces / Contracts

```java
package com.deliveryhub.application.port.in;

import com.deliveryhub.application.dto.RegisterUserRequest;
import com.deliveryhub.domain.model.User;

public interface RegisterUserUseCase {
    /**
     * Registra un nuevo usuario tras validar sus datos y unicidad.
     *
     * @param request datos del usuario
     * @return usuario creado y persistido
     */
    User execute(RegisterUserRequest request);
}
```

```java
package com.deliveryhub.application.port.out;

import com.deliveryhub.domain.model.User;

public interface UserRepositoryPort {
    User save(User user);
    boolean existsByEmail(String email);
    boolean existsByName(String name);
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | Successful registration | Mock `UserRepositoryPort` returning false for exists checks, verify save is called and user is returned. |
| Unit | Duplicate email validation | Mock `existsByEmail` to return true, assert `UserAlreadyExistsException` is thrown. |
| Unit | Duplicate name validation | Mock `existsByName` to return true, assert `UserAlreadyExistsException` is thrown. |
| Unit | Invalid input formatting | Request with blank name, blank email, or invalid email format, assert `DomainValidationException` is thrown. |

## Migration / Rollout

No data migration required as we are introducing the application layer and not modifying existing DB schemas.

## Open Questions

- None. The specification is aligned with the existing Domain rules.

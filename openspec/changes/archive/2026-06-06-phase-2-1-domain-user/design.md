# Design: User Domain Entity Implementation

## Technical Approach

Implement the `User` aggregate root as a domain entity in the `com.deliveryhub.domain.model` package. The design follows the project's Hexagonal Architecture, ensuring an "always valid" state through internal validation. We will use static factory methods for creation and reconstruction, similar to the `Order` entity, but with added validation logic as seen in `Address`.

## Architecture Decisions

### Decision: Internal Validation Strategy

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Bean Validation (JSR 380) | Requires external dependencies in domain, often used in infrastructure. | Rejected |
| Manual validation in constructor | Ensures "always valid" state, zero external dependencies. | **Chosen** |
| Validation at Application layer | Domain objects could exist in an invalid state. | Rejected |

**Rationale**: To keep the domain pure and robust, the entity itself must enforce its invariants. Using manual validation in the constructor/factory methods ensures that no invalid `User` object can ever exist in the system.

### Decision: Exception Handling

| Option | Tradeoff | Decision |
|--------|----------|----------|
| `IllegalArgumentException` | Standard Java exception, but lacks domain context. | Rejected |
| `DomainValidationException` | Specific to domain rules, allows for better error handling/mapping. | **Chosen** |

**Rationale**: Following the pattern of `InvalidStatusTransitionException`, a custom `DomainValidationException` will be used to signal domain-specific validation failures, distinguishing them from generic runtime errors.

## Data Flow

Data enters the `User` entity only through static factory methods.

```
(Application Layer) ──> User.create() ───┐
                                         V
(Persistence Adapter) ─> User.reconstruct() ──> [Validation] ──> New User Instance
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/deliveryhub/domain/exception/DomainValidationException.java` | Create | New exception for domain-level validation errors. |
| `src/main/java/com/deliveryhub/domain/model/User.java` | Create | The User aggregate root implementation. |

## Interfaces / Contracts

### User Aggregate Root

```java
public class User {
    private final UUID id;
    private final String name;
    private final String email;
    private final String password; // Pre-hashed
    private final UserRole role;

    private User(UUID id, String name, String email, String password, UserRole role) {
        // Validation logic here
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static User create(String name, String email, String password, UserRole role) {
        return new User(UUID.randomUUID(), name, email, password, role);
    }

    public static User reconstruct(UUID id, String name, String email, String password, UserRole role) {
        return new User(id, name, email, password, role);
    }
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | User Creation | Test successful creation with valid data. |
| Unit | Mandatory Fields | Test that blank name/email or null role throws `DomainValidationException`. |
| Unit | Email Format | Test that email without '@' throws `DomainValidationException`. |
| Unit | Reconstruction | Test that reconstruction correctly maps all fields. |

## Migration / Rollout

No migration required. This is a new domain entity implementation.

## Open Questions

None.

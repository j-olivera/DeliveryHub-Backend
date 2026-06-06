## Exploration: User Entity Requirements

### Current State
The project follows Clean Architecture with a clearly defined Domain layer. Existing entities like `Order` and `Address` set the pattern for how domain models should be implemented (plain Java classes/records with static factories). The `UserRole` enum is already available. The database schema for the `users` table is defined in `docs/database.md`.

### Affected Areas
- `src/main/java/com/deliveryhub/domain/model/User.java` — New domain entity to be implemented.
- `src/main/java/com/deliveryhub/domain/enums/UserRole.java` — Existing enum to be used by the `User` entity.
- `src/main/java/com/deliveryhub/domain/exception/` — Potential new exceptions (e.g., `UserNotFoundException`).

### Approaches
1. **Plain Java Entity (Recommended)** — Implement `User` as a class with a private constructor, static factory methods (`create`, `reconstruct`), and explicit domain logic methods.
   - Pros: Consistent with `Order.java` pattern; adheres to Clean Architecture; allows internal state mutation for domain transitions while maintaining encapsulation.
   - Cons: Requires manual implementation of getters and factory methods (more boilerplate than Lombok).
   - Effort: Low.

2. **Java Record** — Implement `User` as a record.
   - Pros: Minimal boilerplate; built-in immutability.
   - Cons: Inconsistent with `Order.java`; requires the "Wither" pattern for state changes (like deactivating a user), which can be less intuitive for entity state transitions.
   - Effort: Low.

### Recommendation
**Approach 1: Plain Java Entity.** This approach ensures architectural consistency with the existing `Order` entity and provides a clear path for implementing domain logic and state transitions (e.g., `activate()`, `deactivate()`) without framework dependencies.

### Risks
- **Schema Alignment**: The DB column is `name`, while some docs/requests might refer to `fullName`. The domain model should likely use `name` to align with the established schema.
- **Password Handling**: The domain entity will hold the password hash. It's important to ensure no business logic accidentally exposes this.
- **Validation**: Basic validation (non-blank name, valid email format) should be in the domain, but complex rules like email uniqueness must be handled at the application layer (via repository checks).

### Ready for Proposal
Yes. The requirements are well-defined in the documentation and the implementation pattern is clearly established in the codebase.

## Exploration: Phase 2 Domain Layer Implementation

### Current State
Phase 1 has established the project structure and scaffolding. The domain layer packages (`enums`, `model`, `exception`) are created but empty. Documentation (`domain-rules.md`, `class-conventions.md`) provides a clear blueprint for the entity behavior and coding standards.

### Affected Areas
- `src/main/java/com/deliveryhub/domain/model/` — Will contain `Order` and `Address` entities.
- `src/main/java/com/deliveryhub/domain/enums/` — Will contain `OrderStatus`.
- `src/main/java/com/deliveryhub/domain/exception/` — Will contain domain-specific exceptions like `InvalidStatusTransitionException`.

### Approaches
1. **Rich Domain Model (Recommended)** — Business rules and state transitions are implemented directly within the `Order` entity.
   - Pros: Encapsulation, easier to test business rules, prevents invalid states.
   - Cons: Slightly more complex entity logic.
   - Effort: Medium

2. **Anemic Domain Model** — Entities only hold data; logic resides in Services/Use Cases.
   - Pros: Simpler entities.
   - Cons: Business rules are scattered, risk of inconsistent state, violates Clean Architecture principles for domain-driven design.
   - Effort: Low

### Recommendation
Implement a **Rich Domain Model**. The `Order` entity should be the authority on its own state transitions, validating rules defined in `docs/domain-rules.md`. It should use private constructors and static factory methods (`create`, `reconstruct`) to ensure valid instantiation.

### Risks
- **Leaky Abstractions**: Ensuring NO infrastructure/JPA annotations enter the domain layer.
- **Validation Complexity**: Managing sequential transitions strictly (`PENDING` -> `ASSIGNED` -> `IN_PROGRESS` -> `DELIVERED`).
- **UUID Generation**: Deciding if the domain generates the UUID (`create`) or if it's passed from the outside (better for idempotency).

### Ready for Proposal
Yes — The requirements and implementation patterns are clearly defined in the documentation.

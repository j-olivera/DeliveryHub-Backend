# Proposal: Phase 2 - Domain Layer Implementation

## Intent

Implement the core business logic of DeliveryHub in a pure Java Domain layer, establishing the `Order` aggregate and its state machine. This ensures business rules are encapsulated, type-safe, and protected from infrastructure concerns, following Clean Architecture and DDD principles.

## Scope

### In Scope
- `OrderStatus` and `UserRole` enums.
- `Address` value object with coordinate validation (PostGIS-ready).
- `Order` aggregate root with transition logic and validation.
- Domain exceptions: `InvalidStatusTransitionException`, `UnauthorizedTransitionException`.
- Unit tests for domain lifecycle and business rules.

### Out of Scope
- Persistence implementation (JPA Entities/Repositories).
- Use case / Service layer implementation.
- API Controllers and DTOs.

## Capabilities

### New Capabilities
- `order-domain-logic`: The core requirements and scenarios for Order lifecycle, validation, and status transitions.

### Modified Capabilities
- None

## Approach

Rich Domain Model with encapsulated state and validation.
- Use **Static Factory Methods** (`create`, `reconstruct`) to ensure objects are always in a valid state.
- **Strict State Machine**: `Order` status transitions are governed by internal logic, validating against `UserRole` and current status.
- **Value Objects**: Use immutable objects for `Address` to handle geographic concerns.
- **Pure Java**: No framework dependencies (Spring/JPA) in the `domain` package.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/java/com/deliveryhub/domain/model/` | New | `Order.java`, `Address.java` |
| `src/main/java/com/deliveryhub/domain/enums/` | New | `OrderStatus.java`, `UserRole.java` |
| `src/main/java/com/deliveryhub/domain/exception/` | New | `InvalidStatusTransitionException.java`, `UnauthorizedTransitionException.java` |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Inconsistent transitions | Low | Exhaustive unit testing of the state machine. |
| Infrastructure leak | Low | Architecture check to ensure no annotations/imports from Spring/JPA. |

## Rollback Plan

Delete the newly created domain files in `com.deliveryhub.domain` and revert project state to the last clean commit.

## Dependencies

- None (Pure Java Domain)

## Success Criteria

- [ ] `Order` aggregate correctly enforces all state transitions defined in `docs/domain-rules.md`.
- [ ] `Address` value object validates geographic coordinates correctly.
- [ ] 100% test coverage for domain business rules.

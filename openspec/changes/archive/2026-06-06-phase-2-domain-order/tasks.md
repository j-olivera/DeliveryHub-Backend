# Implementation Tasks: Phase 2 - Domain Layer

## Change Information
- **Change Name**: `phase-2-domain-order`
- **Goal**: Implement the core domain model (`Order`, `Address`, `OrderStatus`, `UserRole`) with full TDD coverage.
- **Strategy**: `single-pr`

## Phase 1: Enums
Foundation for type-safe status and role management.

- [x] **Task 1.1**: Create `UserRole` enum.
  - Location: `src/main/java/com/deliveryhub/domain/enums/UserRole.java`
  - Values: `ADMIN`, `DELIVERY`.
- [x] **Task 1.2**: Create `OrderStatus` enum.
  - Location: `src/main/java/com/deliveryhub/domain/enums/OrderStatus.java`
  - Values: `PENDING`, `ASSIGNED`, `IN_PROGRESS`, `DELIVERED`, `CANCELLED`.

## Phase 2: Domain Exceptions
Exceptions for business rule violations.

- [x] **Task 2.1**: Create `InvalidStatusTransitionException`.
  - Location: `src/main/java/com/deliveryhub/domain/exception/InvalidStatusTransitionException.java`
  - Type: `RuntimeException` (Unchecked).
- [x] **Task 2.2**: Create `UnauthorizedTransitionException`.
  - Location: `src/main/java/com/deliveryhub/domain/exception/UnauthorizedTransitionException.java`
  - Type: `RuntimeException` (Unchecked).

## Phase 3: Address Value Object
Immutable record with coordinate and field validation.

- [x] **Task 3.1 [RED]**: Create `AddressTest` to verify validation rules.
  - Test cases: null/blank street/number/city, latitude range (-90 to 90), longitude range (-180 to 180).
- [x] **Task 3.2 [GREEN]**: Implement `Address` record.
  - Location: `src/main/java/com/deliveryhub/domain/model/Address.java`
  - Use constructor validation.

## Phase 4: Order Aggregate Root
Core state machine and business logic.

- [x] **Task 4.1 [RED]**: Test `Order.create()` factory method.
  - Verify initial status is `PENDING`, IDs are set, and timestamps are generated.
- [x] **Task 4.2 [GREEN]**: Implement `Order.create()` and base properties.
- [x] **Task 4.3 [RED]**: Test `Order.reconstruct()` factory method.
  - Verify all fields (including `updatedAt` and `deliveryUserId`) are correctly restored.
- [x] **Task 4.4 [GREEN]**: Implement `Order.reconstruct()`.
- [x] **Task 4.5 [RED]**: Test `assign(deliveryUserId, requesterRole)`.
  - Scenarios: Valid Admin assignment, Non-Admin attempt (fail), Non-Pending status (fail).
- [x] **Task 4.6 [GREEN]**: Implement `assign()`.
- [x] **Task 4.7 [RED]**: Test `startDelivery(requesterId, requesterRole)`.
  - Scenarios: Valid Delivery start, Wrong Delivery User (fail), Not Assigned status (fail), Admin attempt (fail).
- [x] **Task 4.8 [GREEN]**: Implement `startDelivery()`.
- [x] **Task 4.9 [RED]**: Test `completeDelivery(requesterId, requesterRole)`.
  - Scenarios: Valid completion, Not In Progress status (fail), Wrong Delivery User (fail).
- [x] **Task 4.10 [GREEN]**: Implement `completeDelivery()`.
- [x] **Task 4.11 [RED]**: Test `cancel(requesterRole)`.
  - Scenarios: Admin cancels (from Pending/Assigned/InProgress), Non-Admin attempt (fail), From Terminal state (fail).
- [x] **Task 4.12 [GREEN]**: Implement `cancel()`.
- [x] **Task 4.13 [RED]**: Test immutability of terminal states.
  - Verify `DELIVERED` and `CANCELLED` orders cannot be modified or transitioned.
- [x] **Task 4.14 [GREEN]**: Ensure terminal state logic is robust.

## Review Workload Forecast
- **Estimated Changed Lines**: ~400 lines (Core logic is small, but exhaustive TDD tests for 6+ scenarios drive volume).
- **Chained PRs Recommended**: No. The domain model is highly cohesive and should be reviewed as a single unit.
- **Decision needed before apply**: No.
- **400-line budget risk**: Medium. If tests are very verbose, it might cross 400.

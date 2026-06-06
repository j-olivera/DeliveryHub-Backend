# Technical Specification: order-domain-logic

## Description
Core business logic for the Order aggregate, including state machine validation, role-based transitions, and ownership rules.

## Requirements

### 1. Order Creation
- **R1.1**: A new Order SHALL always start in the `PENDING` state.
- **R1.2**: A new Order MUST have a valid customer address.
- **R1.3**: A new Order MUST have an associated customer ID.

### 2. State Machine Validation
- **R2.1**: The Order MUST strictly follow the state machine defined in `docs/domain-rules.md`:
  - `PENDING` → `ASSIGNED`
  - `ASSIGNED` → `IN_PROGRESS`
  - `IN_PROGRESS` → `DELIVERED`
  - `PENDING | ASSIGNED | IN_PROGRESS` → `CANCELLED`
- **R2.2**: Any transition that does not follow the defined path SHALL throw an `InvalidStatusTransitionException`.
- **R2.3**: Terminal states (`DELIVERED`, `CANCELLED`) MUST be immutable. No transitions from terminal states are allowed.

### 3. Role-Based Ownership and Transitions
- **R3.1**: Only a user with the `ADMIN` role SHALL be able to transition an Order from `PENDING` to `ASSIGNED`.
- **R3.2**: Transitioning an Order to `ASSIGNED` MUST require a valid, non-null `deliveryUserId`.
- **R3.3**: Only the `DELIVERY` user assigned to the Order SHALL be able to transition it to `IN_PROGRESS` or `DELIVERED`.
- **R3.4**: Transition attempts by a `DELIVERY` user who is not assigned to the Order MUST throw an `UnauthorizedTransitionException`.
- **R3.5**: Only an `ADMIN` user SHALL be able to transition an Order to `CANCELLED` from any non-terminal state.

### 4. Domain Invariants
- **R4.1**: An Order in `ASSIGNED`, `IN_PROGRESS`, or `DELIVERED` state SHALL NOT be modified except for status transitions.
- **R4.2**: An Order MUST NOT have more than one delivery user assigned at any time.

## Scenarios

### Scenario 1: Initial State Validation
- **GIVEN** a request to create a new Order
- **WHEN** the Order is instantiated via its factory method
- **THEN** its initial status MUST be `PENDING`

### Scenario 2: Admin Assigns Order
- **GIVEN** an Order in `PENDING` status
- **AND** a user with the `ADMIN` role
- **WHEN** the admin assigns the order to a valid delivery user
- **THEN** the Order status MUST change to `ASSIGNED`
- **AND** the `deliveryUserId` MUST be correctly updated

### Scenario 3: Delivery Starts Order
- **GIVEN** an Order in `ASSIGNED` status with `deliveryUserId = 123`
- **AND** a user with ID `123` and role `DELIVERY`
- **WHEN** the user transitions the order to `IN_PROGRESS`
- **THEN** the Order status MUST change to `IN_PROGRESS`

### Scenario 4: Unauthorized Delivery Transition
- **GIVEN** an Order in `ASSIGNED` status with `deliveryUserId = 123`
- **AND** a user with ID `456` and role `DELIVERY`
- **WHEN** the user attempts to transition the order to `IN_PROGRESS`
- **THEN** an `UnauthorizedTransitionException` MUST be thrown

### Scenario 5: Admin Cancels Order
- **GIVEN** an Order in `IN_PROGRESS` status
- **AND** a user with the `ADMIN` role
- **WHEN** the admin cancels the order
- **THEN** the Order status MUST change to `CANCELLED`

### Scenario 6: Attempt Transition from Terminal State
- **GIVEN** an Order in `DELIVERED` status
- **WHEN** any user attempts to change its status to `CANCELLED` (or any other state)
- **THEN** an `InvalidStatusTransitionException` MUST be thrown

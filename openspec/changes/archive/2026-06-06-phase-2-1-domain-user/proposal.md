# Proposal: Implement User Domain Entity

## Intent
Implement the `User` aggregate root in the `com.deliveryhub.domain.model` package. This entity will represent system actors (Admins and Deliveries), providing a central point for user identity, role management, and internal consistency validation.

## Scope
- Implementation of the `User` entity in the Domain layer.
- Support for key fields: `id`, `name`, `email`, `password` (hash), and `role`.
- Static factory methods for creation and reconstruction from persistence.
- Domain-level validation for name, email format, and role.

## Goals
- Provide a rich domain model for User following the project's Hexagonal Architecture.
- Ensure all users have valid emails and assigned roles upon creation.
- Encapsulate user state and transitions.

## Approach
The `User` class will be a POJO in the domain layer, similar to `Order.java`. It will use a private constructor to force usage of static factory methods (`create`, `reconstruct`). Validation will be performed within the domain model to ensure an "always valid" state for the aggregate.

## Capabilities
### user-domain-logic
- **Requirement**: Create a user with a unique ID, name, email, password, and role.
- **Requirement**: Validate that name and email are not blank.
- **Requirement**: Validate basic email format (must contain '@').
- **Requirement**: Support reconstruction from database state.

## Dependencies
- `com.deliveryhub.domain.enums.UserRole`
- `java.util.UUID`

## Risks
- **Password Security**: The domain model will handle the password as a hashed string; however, hashing logic must be kept in the application/infrastructure layer to keep the domain pure.
- **Email Uniqueness**: While the domain can validate format, uniqueness must be enforced at the application or infrastructure (database) level.

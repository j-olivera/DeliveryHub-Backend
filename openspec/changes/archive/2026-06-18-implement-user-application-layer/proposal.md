# Proposal: Implement User Application Layer

## Intent

Provide the application layer interface and implementation for user registration, enforcing business rules (unique email and name/username) and delegating user persistence.

## Scope

### In Scope
- Define `RegisterUserUseCase` (input port) and `UserRepositoryPort` (output port).
- Implement `RegisterUserRequest` (application DTO).
- Implement `RegisterUserUseCaseImpl` coordinating validations and persistence.
- Define `UserAlreadyExistsException` (domain-level validation exception).
- Create comprehensive unit tests for the use case implementation.

### Out of Scope
- Database migrations, JPA mappings, and controller endpoints (REST).
- Password encoding implementation (deferred to security config in presentation/infrastructure).

## Capabilities

### New Capabilities
- `user-registration`: contract and flow for registering new users, including unique username and email validations and persistence delegation.

### Modified Capabilities
- None

## Approach

1. Create application DTO `RegisterUserRequest`.
2. Define input port `RegisterUserUseCase` and output port `UserRepositoryPort`.
3. Implement `UserAlreadyExistsException` extending `DomainValidationException`.
4. Implement `RegisterUserUseCaseImpl` executing:
   - Duplicate checks using repository port.
   - User creation delegating to the `User` domain entity.
   - Persistence delegating to repository port.
5. Apply TDD following project testing rules with JUnit 5 and Mockito.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/java/com/deliveryhub/domain/exception/UserAlreadyExistsException.java` | New | Domain-level validation exception for duplicates |
| `src/main/java/com/deliveryhub/application/dto/RegisterUserRequest.java` | New | Application DTO for registration payload |
| `src/main/java/com/deliveryhub/application/port/in/RegisterUserUseCase.java` | New | Input port interface |
| `src/main/java/com/deliveryhub/application/port/out/UserRepositoryPort.java` | New | Output port interface |
| `src/main/java/com/deliveryhub/application/usecase/RegisterUserUseCaseImpl.java` | New | Use case implementation with validations |
| `src/test/java/com/deliveryhub/application/usecase/RegisterUserUseCaseTest.java` | New | Unit tests for implementation |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Concurrency race condition | Low | Implement unique DB constraints in the DB phase |

## Rollback Plan

Revert git changes and remove created application/domain files.

## Dependencies

- Domain model `User` and enum `UserRole` (pre-existing)

## Success Criteria

- [ ] New user registration successfully persists the user.
- [ ] Attempting duplicate email/username throws `UserAlreadyExistsException`.
- [ ] Invalid email/name formats are rejected.

## Exploration: Implement the Application Layer (Phase 3) for User

### Current State
Today, the project has the domain model `User` and `UserRole` in the domain layer, along with standard domain exceptions. The application layer (`com.deliveryhub.application`) contains the sub-package structure (`port/in`, `port/out`, `usecase`, etc.) but does not contain any functional interfaces or implementation classes yet. To register a user, we need to create the ports and use cases that orchestrate the logic, validate unique usernames and emails, and delegate saving to a repository port.

### Affected Areas
No existing source code files will be modified, as this is a greenfield implementation of the application layer. The following new files will be created:
- `src/main/java/com/deliveryhub/domain/exception/UserAlreadyExistsException.java` — new domain exception representing duplicate username or email violations.
- `src/main/java/com/deliveryhub/application/port/out/UserRepositoryPort.java` — output port defining query and save contracts for the user data store.
- `src/main/java/com/deliveryhub/application/port/in/RegisterUserUseCase.java` — input port defining the signature for registering a user.
- `src/main/java/com/deliveryhub/application/usecase/RegisterUserUseCaseImpl.java` — implementation of the input port containing the coordination logic and validations.
- `src/test/java/com/deliveryhub/application/usecase/RegisterUserUseCaseTest.java` — unit tests for the use case verifying successful creation and validation exceptions.

### Approaches

#### Interface Design Options

1. **Approach A: Direct parameters in the input port (`execute(String name, String email, String password, UserRole role)`)**
   - Pros:
     - Extremely simple and direct.
     - Decouples the usecase interface from any DTO definitions, conforming to strict Clean Architecture.
     - Fits standard patterns for simple ports in the existing class conventions (e.g. `AssignOrderUseCase`).
   - Cons:
     - Signature would grow if more fields are added to User registration in the future.
   - Effort: Low

2. **Approach B: Usecase-specific Command DTO (`RegisterUserCommand`)**
   - Pros:
     - Scales easily if more fields are added later.
   - Cons:
     - Adds extra boilerplate classes when there are only 4 parameters.
     - Usecases returning or receiving application-specific DTOs can introduce unnecessary complexity at this stage.
   - Effort: Medium

#### Validation Design Options

1. **Approach 1: Pre-validation using Repository Queries (`existsByName`, `existsByEmail`) in Use Case**
   - Pros:
     - Explicit validation alignment with domain rules prior to persistence.
     - Decoupled from specific database vendor exceptions (keeping application layer pure).
   - Cons:
     - Potential race condition under concurrent registrations (mitigated by database-level unique constraints).
   - Effort: Low

2. **Approach 2: Relying on database exceptions (`DataIntegrityViolationException`)**
   - Pros:
     - Avoids race conditions.
   - Cons:
     - Strongly couples the application or domain layer with Spring-specific DB exceptions.
     - Parsing exception messages to distinguish between duplicate name and email is error-prone.
   - Effort: Medium

### Recommendation
We recommend **Approach A** (Direct parameters in input port) and **Approach 1** (Pre-validation using repository methods). This is the cleanest approach, aligns with project design rules, and keeps the domain validations explicit and easy to test at the use-case layer. A database unique constraint will still exist at the database level (in a later phase) to prevent race conditions.

### Risks
- **Concurrency race condition**: If two users attempt to register the exact same username or email simultaneously, the `existsBy...` checks might pass for both, leading to database errors.
  *Mitigation*: Ensure the database layer (Phase 4) applies `UNIQUE` indexes on columns `name` and `email` to act as a fallback guard.

### Ready for Proposal
Yes. The requirements and architecture are clear. The proposal can be constructed to define the exact classes, packages, and unit tests to implement.

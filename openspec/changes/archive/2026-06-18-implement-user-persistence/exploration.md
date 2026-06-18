## Exploration: implement-user-persistence

### Current State
Currently, the application layer defines `UserRepositoryPort` for database actions, and the domain layer defines `User` and `UserRole`. However, there is no infrastructure layer implementation, no JPA entities, no Spring Data repositories, and no active database migrations. The application tests use in-memory configurations or mocks.

### Affected Areas
- `src/main/resources/db/migration/V1__create_users_table.sql` — New Flyway migration to define the users schema.
- `src/main/java/com/deliveryhub/infrastructure/persistence/entity/UserEntity.java` — New JPA entity matching the schema.
- `src/main/java/com/deliveryhub/infrastructure/persistence/UserJpaRepository.java` — New Spring Data JPA Repository.
- `src/main/java/com/deliveryhub/infrastructure/persistence/UserRepositoryAdapter.java` — New implementation of the `UserRepositoryPort`.
- `src/main/java/com/deliveryhub/infrastructure/mapper/UserMapper.java` — New component to map between `User` domain model and `UserEntity`.
- `src/test/java/com/deliveryhub/infrastructure/persistence/UserRepositoryAdapterTest.java` — New repository tests verifying JPA queries, saves, and constraint violations.

### Approaches
1. **Idiomatic Custom Mapper Interface (Project Convention)** — Define `UserMapper` as a `@Component` interface with default Java methods for mappings, exactly as defined in `docs/class-conventions.md`.
   - Pros: Directly aligns with the existing project documentation, avoids classpath pollution or complex annotation processor setups in Maven.
   - Cons: Requires manual mapping methods.
   - Effort: Low

2. **Standard MapStruct Integration** — Introduce MapStruct dependency in `pom.xml` and generate implementations automatically via compiler annotation processors.
   - Pros: MapStruct handles mapping boilerplate automatically.
   - Cons: Adds external dependency and requires editing `pom.xml` to include mapstruct and mapstruct-processor, which increases compilation complexity.
   - Effort: Medium

### Recommendation
Approach 1 is recommended. It conforms directly to the guidelines in `docs/class-conventions.md` (specifically under Mapper (MapStruct)), where mappers are designed as `@Component` interfaces with default methods. This keeps the project build clean and prevents compile-time processor conflicts.

### Risks
- **UUID Function Compatibility**: The PostgreSQL migration will use `uuid_generate_v4()`. H2 (which runs in `MODE=PostgreSQL` for testing) does not natively support `uuid_generate_v4()` unless it is explicitly stubbed or if UUIDs are generated on the Java side. Since `User.create` generates UUIDs on the Java side using `UUID.randomUUID()`, this is mitigated, but the schema definition should still handle PostgreSQL defaults gracefully.
- **Unique Constraint Violations**: Flyway execution in integration tests must be properly managed (since Flyway is currently disabled in tests, we will verify the schema structure and constraints in the integration tests using the JPA configuration).

### Ready for Proposal
Yes — The codebase is fully prepared for implementing the persistence layer for the `User` entity, and we can proceed to the change proposal.

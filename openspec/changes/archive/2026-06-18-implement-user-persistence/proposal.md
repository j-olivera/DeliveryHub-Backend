# Proposal: Implement User Persistence

## Intent

We need to implement the persistence layer for the `User` aggregate root to enable registration and query operations in a real PostgreSQL database. Currently, user registration exists only in memory or mocked in tests. This change implements the database schema, the JPA mappings, the mapper, and the adapter following the Clean Architecture ports and adapters pattern.

## Scope

### In Scope
- Create the Flyway database migration `V1__create_users_table.sql` under `src/main/resources/db/migration/`.
- Create `UserEntity` JPA entity mapping the domains to the database.
- Create `UserJpaRepository` interface inheriting from `JpaRepository`.
- Create `UserMapper` for converting between `User` (domain model) and `UserEntity` (JPA entity).
- Create `UserRepositoryAdapter` implementing `UserRepositoryPort`.
- Create `UserRepositoryAdapterTest` integration test suite using `@DataJpaTest`.

### Out of Scope
- Implementing controller endpoints or security validation rules (to be handled in future/separate tasks).
- User password hashing in this layer (which belongs to presentation/application security).

## Capabilities

### New Capabilities
- None

### Modified Capabilities
- `user-domain-logic`: Requirements for persistent storage are added.

## Approach

We will follow Clean Architecture:
1. Write the database migration `V1__create_users_table.sql` based on the schema specified in `docs/database.md`.
2. Define `UserEntity` inside `com.deliveryhub.infrastructure.persistence.entity` package.
3. Define `UserJpaRepository` extending `JpaRepository` to perform standard and custom queries (`existsByEmail`, `existsByName`).
4. Write `UserMapper` using the project's custom mapping convention (a `@Component` interface with default methods) to map the domain `User` to `UserEntity` and vice versa.
5. Implement `UserRepositoryAdapter` implementing `UserRepositoryPort` in `com.deliveryhub.infrastructure.persistence`.
6. Write integration tests in `UserRepositoryAdapterTest` leveraging `@DataJpaTest` with test profile database configs.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/resources/db/migration` | New | Database migration script for users table |
| `src/main/java/com/deliveryhub/infrastructure/persistence` | New | Repository adapter, JPA entity, JPA repository |
| `src/main/java/com/deliveryhub/infrastructure/mapper` | New | Component mapper for User |
| `src/test/java/com/deliveryhub/infrastructure/persistence` | New | Integration tests for the repository adapter |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Database migration compatibility issues between H2 (tests) and PostgreSQL (production) | Low | Use standard SQL check constraints and types compatible with both, verifying in tests |

## Rollback Plan

To roll back:
1. Revert/delete the files created in this PR.
2. If the migration was executed in database, run a script to drop the `users` table and delete the entry from `flyway_schema_history`.

## Dependencies

- PostgreSQL driver and Spring Data JPA dependencies (already in `pom.xml`).

## Success Criteria

- [ ] Successful application of Flyway database migration `V1__create_users_table.sql` on test runs.
- [ ] JPA entity mappings verified through successful save and retrieval tests.
- [ ] Integration tests in `UserRepositoryAdapterTest` passing successfully.

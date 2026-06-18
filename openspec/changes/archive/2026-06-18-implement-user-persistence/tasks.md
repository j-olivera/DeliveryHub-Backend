# Tasks: Implement User Persistence

## Phase 1: Database Foundation

- [x] 1.1 Create Flyway schema migration `src/main/resources/db/migration/V1__create_users_table.sql` for the `users` table.

## Phase 2: User Persistence Infrastructure (TDD)

- [x] 2.1 Write failing integration test in `src/test/java/com/deliveryhub/infrastructure/persistence/UserRepositoryAdapterTest.java` to save and retrieve a user (RED).
- [x] 2.2 Create JPA Entity `src/main/java/com/deliveryhub/infrastructure/persistence/entity/UserEntity.java` with JPA mappings.
- [x] 2.3 Create Spring Data repository `src/main/java/com/deliveryhub/infrastructure/persistence/UserJpaRepository.java` extending `JpaRepository`.
- [x] 2.4 Create manual mapper `src/main/java/com/deliveryhub/infrastructure/mapper/UserMapper.java` to map between `User` and `UserEntity`.
- [x] 2.5 Create repository adapter `src/main/java/com/deliveryhub/infrastructure/persistence/UserRepositoryAdapter.java` implementing `UserRepositoryPort`.
- [x] 2.6 Run integration test and verify that save and retrieve passes (GREEN).
- [x] 2.7 Write failing tests in `UserRepositoryAdapterTest` for `existsByEmail` and `existsByName` (RED).
- [x] 2.8 Implement `existsByEmail` and `existsByName` in `UserJpaRepository` and `UserRepositoryAdapter` to make the tests pass (GREEN).
- [x] 2.9 Refactor code in `UserRepositoryAdapter`, `UserMapper`, and tests for clean coding standards (REFACTOR).

## Phase 3: Integration & Verification

- [x] 3.1 Run `mvn clean verify` to ensure all tests pass and check style rules.

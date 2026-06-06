# Tasks: User Domain Entity Implementation

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 250-300 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | single PR |
| Delivery strategy | single-pr |
| Chain strategy | size-exception |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: Low

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Implement User Domain Model and Exceptions | PR 1 | Base implementation with full TDD coverage |

## Phase 1: Foundation (Exceptions)

- [x] 1.1 Create `com.deliveryhub.domain.exception.DomainValidationException` extending `RuntimeException`
- [x] 1.2 Add constructor to `DomainValidationException` accepting a message string
- [x] 1.3 Verify exception can be instantiated in a smoke test

## Phase 2: User Entity - Creation & Validation (Strict TDD)

- [x] 2.1 **RED**: Write test for successful `User.create()` in `com.deliveryhub.domain.model.UserTest`
- [x] 2.2 **GREEN**: Implement `User` class with `create` factory and private constructor to pass creation test
- [x] 2.3 **REFACTOR**: Ensure `User` fields are final and naming follows project standards
- [x] 2.4 **RED**: Write tests for mandatory field validation (blank name, blank email, null role) throwing `DomainValidationException`
- [x] 2.5 **GREEN**: Implement validation logic in `User` constructor to pass mandatory field tests
- [x] 2.6 **RED**: Write test for invalid email format (missing '@') throwing `DomainValidationException`
- [x] 2.7 **GREEN**: Implement email format validation in `User` constructor
- [x] 2.8 **REFACTOR**: Consolidate validation logic into private helper methods within `User` if necessary

## Phase 3: User Entity - Reconstruction (Strict TDD)

- [x] 3.1 **RED**: Write test for `User.reconstruct()` ensuring all fields are correctly mapped from provided state
- [x] 3.2 **GREEN**: Implement `reconstruct` factory method in `User`
- [x] 3.3 **REFACTOR**: Ensure `create` and `reconstruct` share the same validation path via the private constructor

## Phase 4: Verification & Documentation

- [x] 4.1 Run all domain tests to ensure no regressions
- [x] 4.2 Verify Javadoc for `User` and `DomainValidationException`
- [x] 4.3 Ensure `User` class is properly encapsulated (getters only, no setters)

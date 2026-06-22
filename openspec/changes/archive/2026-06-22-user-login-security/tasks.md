# Tasks: User Login Security

## Review Workload Forecast
Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: Medium

## Phase 1: Foundation
- [x] Add `spring-boot-starter-security` and `jjwt` dependencies to `pom.xml`.
- [x] Create `PasswordEncoderPort` in `application/port/out/`.
- [x] Write unit test for `PasswordEncoderAdapter`.
- [x] Implement `PasswordEncoderAdapter` in `infrastructure/security/` using BCrypt.
- [x] Write unit test for `JwtUtils`.
- [x] Implement `JwtUtils` in `infrastructure/security/` for token generation/validation.

## Phase 2: Login Case & Auth
- [x] Create `LoginResponse` DTO in `application/dto/`.
- [x] Create `LoginUseCase` port in `application/port/in/`.
- [x] Write unit tests for `LoginUseCaseImpl` verifying credentials checking and JWT generation.
- [x] Implement `LoginUseCaseImpl` in `application/usecase/`.
- [x] Write `WebMvcTest` for `AuthController`.
- [x] Implement `AuthController` in `presentation/rest/` handling login request and returning `LoginResponse`.

## Phase 3: Password Hash Migration
- [x] Modify `RegisterUserUseCaseImpl` unit tests to verify passwords are encoded before saving.
- [x] Inject `PasswordEncoderPort` into `RegisterUserUseCaseImpl` and encode passwords during registration.

## Phase 4: Security Integration
- [x] Write unit tests for `UserDetailsServiceImpl`.
- [x] Implement `UserDetailsServiceImpl` in `infrastructure/security/`.
- [x] Write tests for `JwtFilter`.
- [x] Implement `JwtFilter` in `infrastructure/security/`.
- [x] Implement `SecurityConfig` in `infrastructure/security/` protecting routes and registering filter.

## Phase 5: Verification
- [x] Run all unit tests: `mvn test`.
- [x] Run WebMvcTest integration tests.

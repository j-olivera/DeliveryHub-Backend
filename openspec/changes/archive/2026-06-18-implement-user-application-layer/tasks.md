# Tasks: Implement User Application Layer

## Phase 1: Infrastructure and Foundation

- [x] 1.1 Create `src/main/java/com/deliveryhub/domain/exception/UserAlreadyExistsException.java` extending `DomainValidationException`
- [x] 1.2 Create `src/main/java/com/deliveryhub/application/dto/RegisterUserRequest.java` as a Java record with fields: name, email, password, and role
- [x] 1.3 Create the input port interface `src/main/java/com/deliveryhub/application/port/in/RegisterUserUseCase.java` exposing `execute` method
- [x] 1.4 Create the output port interface `src/main/java/com/deliveryhub/application/port/out/UserRepositoryPort.java` with save and exists check methods

## Phase 2: Core Implementation (TDD)

- [x] 2.1 Write failing unit tests in `src/test/java/com/deliveryhub/application/usecase/RegisterUserUseCaseTest.java` (RED phase)
- [x] 2.2 Implement `src/main/java/com/deliveryhub/application/usecase/RegisterUserUseCaseImpl.java` to make the unit tests pass (GREEN phase)
- [x] 2.3 Refactor implementation and test code to optimize and clean up structure (REFACTOR phase)

## Phase 3: Testing and Verification

- [x] 3.1 Run `./mvnw test` to verify spec scenarios: Successful Registration, Duplicate Email, Duplicate Name, and Invalid Email Format
- [x] 3.2 Verify code structure, package rules, and Javadoc comments in Spanish against `AGENTS.md` conventions

# Tasks: Implement User Registration Endpoint

## Phase 1: Infrastructure Foundation

- [x] 1.1 Add `spring-boot-starter-validation` dependency to `pom.xml`

## Phase 2: DTOs & Validation Contracts

- [x] 2.1 Create presentation request DTO `src/main/java/com/deliveryhub/presentation/dto/RegisterUserRequest.java` with JSR-380 validation annotations
- [x] 2.2 Create presentation response DTO `src/main/java/com/deliveryhub/presentation/dto/UserResponse.java` without password
- [x] 2.3 Create standardized error wrapper DTO `src/main/java/com/deliveryhub/presentation/dto/ErrorResponse.java`

## Phase 3: UserController Implementation (TDD Cycles)

- [x] 3.1 **RED**: Create `src/test/java/com/deliveryhub/presentation/rest/UserControllerTest.java` with a failing test for `POST /api/v1/users` verifying 201 Created and fields mapping
- [x] 3.2 **GREEN**: Create controller class `src/main/java/com/deliveryhub/presentation/rest/UserController.java` and map endpoint to delegate to `RegisterUserUseCase`
- [x] 3.3 **RED**: Add failing validation tests to `UserControllerTest.java` for blank fields, invalid email format, and invalid role
- [x] 3.4 **GREEN**: Add `@Valid` validation annotation to the controller request parameter in `UserController.java` to make validation tests pass
- [x] 3.5 **REFACTOR**: Refactor controller and test code for layout, cleanliness, and formatting standards

## Phase 4: Exception Handling (TDD Cycles)

- [x] 4.1 **RED**: Add failing test for duplicate registration in `UserControllerTest.java` to expect `409 Conflict` on `UserAlreadyExistsException`
- [x] 4.2 **GREEN**: Create `src/main/java/com/deliveryhub/presentation/rest/GlobalExceptionHandler.java` mapping `UserAlreadyExistsException` to HTTP 409
- [x] 4.3 **RED**: Add failing test in `UserControllerTest.java` for method argument validation exceptions (expecting HTTP 400 and `ErrorResponse`)
- [x] 4.4 **GREEN**: Implement handler for validation exceptions and `DomainValidationException` in `GlobalExceptionHandler.java` to return HTTP 400 with `ErrorResponse`
- [x] 4.5 **REFACTOR**: Polish class structures, add Spanish Javadoc comments to all new public classes/methods, and remove any temporary code

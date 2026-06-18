## Exploration: Implement Phase 5: Presentation layer REST endpoint for user registration

### Current State
Currently, the application has the Domain (`User`, `UserRole`, domain exceptions) and Application (`RegisterUserUseCase`, `RegisterUserUseCaseImpl`, application DTOs) layers implemented. A persistence adapter `UserRepositoryAdapter` exists in Infrastructure. However, there are no REST controllers or global exception handlers in the Presentation layer. Spring Security is present in the classpath (`spring-boot-starter-security` in `pom.xml`), but there is no custom security configuration bean yet, meaning default basic authentication is active.

### Affected Areas
No existing source code files will be modified, as this phase only introduces new components in the presentation layer and their corresponding tests:
- `src/main/java/com/deliveryhub/presentation/dto/RegisterUserRequest.java` — New presentation Request DTO with Jakarta Validation.
- `src/main/java/com/deliveryhub/presentation/dto/UserResponse.java` — New presentation Response DTO (excluding password).
- `src/main/java/com/deliveryhub/presentation/dto/ErrorResponse.java` — New generic error message wrapper.
- `src/main/java/com/deliveryhub/presentation/rest/UserController.java` — New REST Controller exposing `/api/v1/users`.
- `src/main/java/com/deliveryhub/presentation/rest/GlobalExceptionHandler.java` — New global controller advice to handle domain exceptions.
- `src/test/java/com/deliveryhub/presentation/rest/UserControllerTest.java` — New WebMvcTest for the registration endpoint.

### Approaches

#### 1. Unit Testing with Security Excluded (Recommended)
Disable the auto-configured Spring Security for the controller unit tests by using `@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})`.
- **Pros:**
  - Simple and direct.
  - Tests only the validation and controller mapping logic.
  - Avoids mock security context boilerplate when no custom security configuration exists.
- **Cons:**
  - Does not test authentication/authorization rules (which are not yet implemented anyway).
- **Effort:** Low

#### 2. Mocking Security in Tests
Leave Spring Security auto-configuration enabled in the tests, using Spring Security Test support (e.g., `@WithMockUser` or custom requests) to bypass security filters.
- **Pros:**
  - Tests the default auto-configuration behavior.
- **Cons:**
  - High configuration overhead for testing a default auto-configured state that will eventually change.
  - Unnecessary dependency on default password generation.
- **Effort:** Medium

### Recommendation
Use **Option 1 (Exclude Security in WebMvcTest)**. Since there is no custom security configuration defined yet in Infrastructure, testing the controller logic in isolation is cleaner and follows the Clean Architecture guideline of separating presentation concerns from security configuration.

### Risks
- **Jakarta Validation Dependency:** Ensure that validation annotations match standard Spring Boot behavior. Spring Boot starter-validation might be required if validation doesn't trigger automatically, though Spring Boot starter-web usually includes it or we can import standard annotations. We should verify in implementation.
- **Role Mapping:** The role parameter in the presentation DTO must map correctly to the `UserRole` enum when invoking the use case, throwing a bad request if the role value is invalid.

### Ready for Proposal
Yes — The requirements are clear, and we have established the class locations and approach for the presentation layer.

# Proposal: User Registration REST Endpoint

## Intent
Expose the application-level user registration logic via a RESTful HTTP endpoint. This allows client applications (like the Angular frontend) to register new users in DeliveryHub.

## Scope

### In Scope
- Create presentation-level request/response DTOs: `RegisterUserRequest` (with validation) and `UserResponse`.
- Create a generic `ErrorResponse` for consistent error format.
- Implement `UserController` at `POST /api/v1/users` mapping presentation DTOs to application DTOs.
- Implement `GlobalExceptionHandler` mapping `UserAlreadyExistsException` to `409 Conflict` and validation/domain errors to `400 Bad Request`.
- Write `UserControllerTest` to verify success, validation errors, and conflict scenarios.

### Out of Scope
- Frontend registration form implementation.
- Custom authentication/authorization setup or Spring Security config (JWT).

## Capabilities

### New Capabilities
- None

### Modified Capabilities
- `user-registration`: Extended to specify HTTP REST endpoint mapping (`POST /api/v1/users`), validation constraints on incoming payloads, and standardized JSON error responses.

## Approach
- Add the presentation components inside `com.deliveryhub.presentation`.
- Apply `@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})` for controller unit testing to bypass Spring Security auto-configuration.
- Implement global controller advice to map Domain & Application exceptions cleanly to HTTP response statuses.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `com.deliveryhub.presentation.dto` | New | Request, Response, and Error DTOs |
| `com.deliveryhub.presentation.rest` | New | `UserController` and `GlobalExceptionHandler` |
| `src/test/java/com/deliveryhub/presentation/rest` | New | `UserControllerTest` unit tests |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Missing validation starter | Low | Verify validation trigger in unit tests, add dependency if needed |
| Role deserialization error | Med | Explicit validation on UserRole value, return 400 on parsing failure |

## Rollback Plan
Revert code changes in presentation layer and testing directories using Git: `git checkout HEAD -- src/main/java/com/deliveryhub/presentation src/test/java/com/deliveryhub/presentation`

## Dependencies
- Active user application layer implementation (`RegisterUserUseCase`)

## Success Criteria
- [ ] Endpoint `POST /api/v1/users` registers users with `201 Created` status
- [ ] Blank or malformed inputs return `400 Bad Request`
- [ ] Existing name/email registration requests return `409 Conflict`

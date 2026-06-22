# Proposal: User Login Security

## Description
Provide secure, stateless user authentication using Spring Security, BCrypt, and JSON Web Tokens (JWT) for DeliveryHub.

## Proposed Changes
- **Security Configuration**: Configure Spring Security to disable session creation (stateless session management) and authorize requests.
- **Authentication Endpoint**: Implement `POST /api/v1/auth/login` validating credentials and returning a JWT.
- **Password Hashing**: Adopt BCrypt for encoding user passwords.
- **Role Enforcement**: Secure routes based on `ADMIN` and `DELIVERY` roles.

## Technical Approach & Dependencies

| Dependency | Purpose | Version |
|---|---|---|
| `spring-boot-starter-security` | Framework security support | Spring Boot standard |
| `io.jsonwebtoken:jjwt-api` | JWT generation & parsing API | `0.12.5` |
| `io.jsonwebtoken:jjwt-impl` | JJWT runtime implementation | `0.12.5` |
| `io.jsonwebtoken:jjwt-jackson` | JJWT Jackson JSON parser | `0.12.5` |

### Security Flow
1. User requests `POST /api/v1/auth/login` with email and password.
2. Server verifies email existence and hashes input password with BCrypt to compare.
3. If valid, generates JWT signed with HMAC-SHA256 containing `userId`, `role` (ADMIN/DELIVERY), and expiration.
4. Subsequent requests to protected APIs must include `Authorization: Bearer <token>`. `JwtFilter` parses, validates, and populates the `SecurityContext`.

## Success Criteria
- [ ] User can log in via `POST /api/v1/auth/login` and receive a JWT.
- [ ] Unauthorized requests to protected routes return `401 Unauthorized` or `403 Forbidden`.
- [ ] Requests with valid JWT can access authorized endpoints based on user roles (`ADMIN`, `DELIVERY`).
- [ ] Passwords stored in DB are encoded using BCrypt.

## Rollback Plan
1. Revert Git commits associated with Security configuration, `JwtFilter`, and authentication endpoints.
2. Remove added dependencies from `pom.xml`.
3. If database users schema has been altered to support bcrypt, run a database migration script to restore original password state.

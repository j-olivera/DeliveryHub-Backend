# User Authentication Specification

## Purpose
Defines the contract and behavior for user login and JWT-based request filtering.

## Requirements

### Requirement: User Login Flow
The system MUST expose a REST endpoint at `POST /api/v1/auth/login` to authenticate users. It MUST accept a JSON payload containing `email` and `password`. If credentials are valid, the system MUST return HTTP status `200 OK` with a response payload containing a JWT token (with `userId`, `role`, and expiration claims) and user metadata.

#### Scenario: Successful Login
- GIVEN a valid registered user with email "user@example.com" and password "securePass123"
- WHEN a POST request is sent to "/api/v1/auth/login" with these credentials
- THEN the system MUST return status code 200 OK
- AND the response MUST include a valid JWT token
- AND the JWT MUST contain the `userId`, `role`, and expiration claims
- AND the response MUST contain user metadata (id, email, name, role).

#### Scenario: Failed Login - Invalid Credentials
- GIVEN a request with an email or password that does not match any active user
- WHEN a POST request is sent to "/api/v1/auth/login"
- THEN the system MUST return status code 401 Unauthorized
- AND the response body MUST contain a standardized error message.

### Requirement: JWT Security Filter
Any protected REST endpoint MUST validate the JWT token provided in the HTTP `Authorization` header.

#### Scenario: Request with Valid Token
- GIVEN a protected endpoint
- AND a request header `Authorization: Bearer <valid_token>`
- WHEN the request is received
- THEN the system MUST authorize and process the request.

#### Scenario: Request with Missing or Invalid Token
- GIVEN a protected endpoint
- AND a missing or invalid `Authorization` header
- WHEN the request is received
- THEN the system MUST return status code 401 Unauthorized or 403 Forbidden.

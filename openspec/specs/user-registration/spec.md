# User Registration Specification

## Purpose
Defines the application-level contract and workflow for registering new users, validating inputs, enforcing uniqueness rules, and persisting the user.

## Requirements

### Requirement: Register User Flow
The system MUST expose a REST endpoint at `POST /api/v1/users` to register a user. It MUST accept a JSON payload with name, email, password, and role. The request MUST be validated, and if valid, delegated to the `RegisterUserUseCase` interface. Upon success, the system MUST return HTTP status `201 Created` with a `UserResponse` body containing the ID, name, email, and role.

#### Scenario: Successful Registration
- GIVEN a valid HTTP request payload with name "Alice", email "alice@example.com", password "pwd123", and role "CUSTOMER"
- AND the email and name do not exist in the system
- WHEN a POST request is sent to "/api/v1/users"
- THEN the system MUST return status code 201 Created
- AND the response body MUST contain the created user details (id, name, email, role)
- AND the password MUST NOT be returned in the response.

### Requirement: Registration Uniqueness
The system MUST verify that the email and name are unique prior to user persistence. If the email or name is already associated with an existing user, the registration endpoint MUST return HTTP status `409 Conflict` containing a standardized error response.

| Checked Field | Existing Value | Expected HTTP Status | Expected Error Type |
|---|---|---|---|
| Email | Duplicate | 409 Conflict | UserAlreadyExistsException |
| Name | Duplicate | 409 Conflict | UserAlreadyExistsException |

#### Scenario: Duplicate Email
- GIVEN an HTTP request payload with email "alice@example.com"
- AND a user with email "alice@example.com" already exists
- WHEN a POST request is sent to "/api/v1/users"
- THEN the system MUST return status code 409 Conflict
- AND the response body MUST contain a standardized error payload indicating that the email is already in use.

#### Scenario: Duplicate Name
- GIVEN an HTTP request payload with name "Alice"
- AND a user with name "Alice" already exists
- WHEN a POST request is sent to "/api/v1/users"
- THEN the system MUST return status code 409 Conflict
- AND the response body MUST contain a standardized error payload indicating that the name is already in use.

### Requirement: Input Validation
The system MUST validate the HTTP request payload before processing. The name, email, password, and role MUST NOT be blank/null, email MUST have a valid format, and the role MUST be a valid `UserRole`. If any validation fails, the system MUST return HTTP status `400 Bad Request` with a standardized validation error payload.

#### Scenario: Invalid Email Format
- GIVEN an HTTP request payload with email "invalid-email"
- WHEN a POST request is sent to "/api/v1/users"
- THEN the system MUST return status code 400 Bad Request
- AND the response body MUST contain a validation error payload.

#### Scenario: Blank Fields
- GIVEN an HTTP request payload with blank name, email, or password
- WHEN a POST request is sent to "/api/v1/users"
- THEN the system MUST return status code 400 Bad Request
- AND the response body MUST contain a validation error payload listing the invalid fields.

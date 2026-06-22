# User Registration Delta Specification

## Requirements

### Requirement: Register User Flow
The system MUST expose a REST endpoint at `POST /api/v1/users` to register a user. It MUST accept a JSON payload with name, email, password, and role. The request MUST be validated, and if valid, delegated to the `RegisterUserUseCase` interface. Upon success, the system MUST return HTTP status `201 Created` with a `UserResponse` body containing the ID, name, email, and role. The password MUST be hashed using BCrypt before persistence in the database.

#### Scenario: Successful Registration
- GIVEN a valid HTTP request payload with name "Alice", email "alice@example.com", password "pwd123", and role "CUSTOMER"
- AND the email and name do not exist in the system
- WHEN a POST request is sent to "/api/v1/users"
- THEN the system MUST return status code 201 Created
- AND the response body MUST contain the created user details (id, name, email, role)
- AND the password MUST NOT be returned in the response
- AND the password stored in the database MUST be hashed using BCrypt.

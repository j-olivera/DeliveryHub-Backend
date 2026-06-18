# User Registration Specification

## Purpose
Defines the application-level contract and workflow for registering new users, validating inputs, enforcing uniqueness rules, and persisting the user.

## Requirements

### Requirement: Register User Flow
The system MUST provide an interface `RegisterUserUseCase` with a `register` method. It MUST accept a payload with name, email, raw password, and role, validate them, instantiate a domain `User`, check uniqueness, and persist the user.

#### Scenario: Successful Registration
- GIVEN a valid RegisterUserRequest with name "Alice", email "alice@example.com", password "pwd123", and role "CUSTOMER"
- AND the email and name do not exist in the system
- WHEN registration is requested
- THEN a User entity MUST be created and persisted via UserRepositoryPort
- AND the registered User details returned.

### Requirement: Registration Uniqueness
The system MUST verify that the email and name are unique prior to user persistence. If the email or name is already associated with an existing user, registration MUST fail.

| Checked Field | Existing Value | Expected Error |
|---|---|---|
| Email | Duplicate | UserAlreadyExistsException |
| Name | Duplicate | UserAlreadyExistsException |

#### Scenario: Duplicate Email
- GIVEN a RegisterUserRequest with email "alice@example.com"
- AND a user with email "alice@example.com" already exists
- WHEN registration is requested
- THEN the system MUST throw UserAlreadyExistsException.

#### Scenario: Duplicate Name
- GIVEN a RegisterUserRequest with name "Alice"
- AND a user with name "Alice" already exists
- WHEN registration is requested
- THEN the system MUST throw UserAlreadyExistsException.

### Requirement: Input Validation
The system MUST validate input parameters before creating the user. Name and email MUST NOT be blank, and email MUST have a valid format.

#### Scenario: Invalid Email Format
- GIVEN a RegisterUserRequest with email "invalid-email"
- WHEN registration is requested
- THEN the system MUST fail with validation error.

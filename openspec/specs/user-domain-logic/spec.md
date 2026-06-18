# User Domain Logic Specification

## Purpose
Defines the core domain logic for the `User` aggregate root, ensuring that all user entities within the system maintain a valid and consistent state.

## Requirements

### Requirement: User Creation
The system MUST provide a way to create a `User` entity with a unique identifier, name, email, password hash, and role.

#### Scenario: Successful User Creation
- GIVEN a valid UUID
- AND a non-blank name "John Doe"
- AND a valid email "john@example.com"
- AND a password hash "hashed_password"
- AND a valid UserRole "DELIVERY"
- WHEN the user creation is requested
- THEN a User entity MUST be successfully created with all provided values.

### Requirement: Mandatory Fields Validation
All mandatory fields MUST be present and valid during User creation.

#### Scenario: Name Cannot Be Blank
- GIVEN a valid UUID
- AND a blank name ""
- AND a valid email "john@example.com"
- AND a password hash "hashed_password"
- AND a valid UserRole "ADMIN"
- WHEN the user creation is requested
- THEN the system MUST fail with a validation error indicating the name cannot be blank.

#### Scenario: Email Cannot Be Blank
- GIVEN a valid UUID
- AND a valid name "John Doe"
- AND a blank email ""
- AND a password hash "hashed_password"
- AND a valid UserRole "ADMIN"
- WHEN the user creation is requested
- THEN the system MUST fail with a validation error indicating the email cannot be blank.

#### Scenario: Invalid Email Format
- GIVEN a valid UUID
- AND a valid name "John Doe"
- AND an invalid email "invalid-email"
- AND a password hash "hashed_password"
- AND a valid UserRole "ADMIN"
- WHEN the user creation is requested
- THEN the system MUST fail with a validation error indicating the email format is invalid.

#### Scenario: Role Cannot Be Null
- GIVEN a valid UUID
- AND a valid name "John Doe"
- AND a valid email "john@example.com"
- AND a password hash "hashed_password"
- AND a null UserRole
- WHEN the user creation is requested
- THEN the system MUST fail with a validation error indicating the role is mandatory.

### Requirement: Domain State Reconstruction
The system MUST support the reconstruction of a `User` entity from existing data (e.g., from a database) without triggering creation-only validations or logic.

#### Scenario: Successful User Reconstruction
- GIVEN an existing UUID
- AND an existing name "John Doe"
- AND an existing email "john@example.com"
- AND an existing password hash "hashed_password"
- AND an existing UserRole "ADMIN"
- WHEN the user is reconstructed from state
- THEN a User entity MUST be successfully created matching the provided state.

### Requirement: Persistent Storage

The system SHALL support persisting and retrieving `User` aggregates via the database.

#### Scenario: Successful Save and Retrieve
- GIVEN a valid reconstructed or new User entity
- WHEN the repository save operation is requested
- THEN the user MUST be stored and retrievable with identical fields.

### Requirement: Existence Verification

The repository SHALL check for duplicate email or name against database records.

| Field | Check Method | Returns |
|---|---|---|
| email | `existsByEmail` | true if email exists, else false |
| name | `existsByName` | true if name exists, else false |

#### Scenario: Check Email Existence
- GIVEN a user with email "john@example.com" exists in the database
- WHEN checking if email "john@example.com" exists
- THEN the check MUST return true.

#### Scenario: Check Name Existence
- GIVEN a user with name "John Doe" exists in the database
- WHEN checking if name "John Doe" exists
- THEN the check MUST return true.


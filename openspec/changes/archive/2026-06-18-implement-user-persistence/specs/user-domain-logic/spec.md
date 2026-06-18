# Delta for User Domain Logic

## ADDED Requirements

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

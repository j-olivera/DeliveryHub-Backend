# Spring Boot Configuration Specification

## Purpose
Standardize application configuration using YAML and define baseline settings for database connectivity and security.

## Requirements

### Requirement: YAML Migration
The application MUST use `src/main/resources/application.yml` for configuration instead of `application.properties`.

#### Scenario: Config Loading
GIVEN the application is starting
WHEN the runtime environment is initialized
THEN the `application.yml` file MUST be loaded as the primary configuration source.

### Requirement: Database Configuration
The application MUST configure PostgreSQL connectivity using environment variable overrides for sensitive data.

#### Scenario: PostgreSQL Connection
GIVEN the application is deployed in a container
WHEN it attempts to connect to the database
THEN it MUST use the following properties (or env var equivalents):
- `spring.datasource.url`: `jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:deliveryhub}`
- `spring.datasource.username`: `${DB_USER:postgres}`
- `spring.datasource.password`: `${DB_PASSWORD:postgres}`
- `spring.datasource.driver-class-name`: `org.postgresql.Driver`

### Requirement: Persistence and Migrations
The application MUST have Flyway enabled for database migrations and Hibernate DDL-auto set to `validate` or `none` for non-local environments.

#### Scenario: Flyway Initialization
GIVEN the application starts for the first time
WHEN Flyway is triggered
THEN it MUST look for migrations in `db/migration` and execute them against the configured datasource.

### Requirement: Security Placeholders
The configuration SHOULD include placeholders for JWT settings to be implemented in future phases.

#### Scenario: JWT Config
GIVEN the baseline configuration
WHEN checking the `application.yml`
THEN it SHOULD contain a `security.jwt` section with placeholders for `secret`, `expiration`, and `issuer`.

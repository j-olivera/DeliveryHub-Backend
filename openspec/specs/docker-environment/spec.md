# Docker Environment Specification

## Purpose
Provide a containerized environment for consistent local development and production-ready deployments.

## Requirements

### Requirement: Multi-stage Dockerfile
The project MUST include a `Dockerfile` using multi-stage builds to optimize the final image size and security.

#### Scenario: Build and Run Stages
GIVEN a Docker build command
WHEN the build process executes
THEN it MUST use:
1. A **build stage** with JDK 21 and Maven to compile the application.
2. A **run stage** with a lightweight JRE 21 (e.g., eclipse-temurin) to execute the JAR.

### Requirement: Local Infrastructure
A `docker-compose.yml` file MUST be provided to orchestrate local infrastructure services.

#### Scenario: Database Service
GIVEN `docker-compose up` is executed
WHEN the services start
THEN a `postgres:16-alpine` service MUST be initialized with:
- Persistent volume mapping for data.
- A healthcheck to verify database readiness.
- Environment variables for default database and user.

### Requirement: Database Management
The local environment SHOULD include a pgAdmin service for database inspection.

#### Scenario: pgAdmin Access
GIVEN the docker-compose environment is running
WHEN a developer accesses the configured pgAdmin port
THEN they SHOULD be able to log in and connect to the local PostgreSQL instance.

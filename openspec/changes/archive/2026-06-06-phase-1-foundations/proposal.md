# Proposal: Phase 1 Foundations

## Intent
Establish the technical foundation for DeliveryHub by implementing a Clean Architecture package structure, configuring the application environment via YAML, and providing Docker orchestration for local development.

## Scope
### In Scope
- Scaffold the `com.deliveryhub` package structure (`domain`, `application`, `presentation`, `infrastructure`).
- Migrate `application.properties` to `application.yml` with baseline configurations (PostgreSQL, JPA, Flyway).
- Create a multi-stage `Dockerfile` for Java 21.
- Create `docker-compose.yml` with PostgreSQL and pgAdmin services.

### Out of Scope
- Implementation of specific domain entities or use cases.
- CI/CD pipeline configuration.
- Unit or integration tests for infrastructure.

## Capabilities
### New Capabilities
- Containerized development environment.
- Standardized architectural layers for future development.
- Centralized YAML configuration.

### Modified Capabilities
- Project structure moved from flat to layered.

## Approach
1. **Refactor Structure**: Create directories for Clean Architecture layers and ensure `Application.java` is at the root for component scanning.
2. **Configuration**: Replace `application.properties` with a structured `application.yml` using environment variables for sensitive data.
3. **Containerization**: Implement a multi-stage `Dockerfile` for optimized builds and a `docker-compose.yml` for local infrastructure.

## Affected Areas
- `src/main/java/com/deliveryhub/` (Package restructuring)
- `src/main/resources/` (Configuration migration)
- Project root (Docker files addition)

## Risks
- **Experimental Spring Boot Version**: The `pom.xml` uses 4.0.6. We will monitor for any deviations from standard Spring Boot 3.x behavior.
- **Startup Race Conditions**: The app may attempt to connect to PostgreSQL before it's ready in Docker.

## Rollback Plan
- Revert directory changes and file movements via `git checkout` / `git reset`.
- Delete `Dockerfile` and `docker-compose.yml`.
- Restore `application.properties` from Git history.

## Dependencies
- Docker and Docker Compose (Local environment).
- Java 21 (Maven toolchain).

## Success Criteria
- Application starts without errors with the new structure.
- PostgreSQL and pgAdmin services are accessible via Docker.
- Multi-stage build successfully produces a production-ready JRE image.

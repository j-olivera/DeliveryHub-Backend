# Exploration: Phase 1 Foundations

## Goal
Establish the initial architecture and environment for DeliveryHub backend following Clean Architecture principles and Docker containerization.

## Analysis of Current State
- **Project Type**: Spring Boot 4.0.6, Java 21, Maven.
- **Current Structure**: Flat package `com.deliveryhub` with only `Application.java`.
- **Infrastructure**: Dependencies for PostgreSQL, JPA, Flyway, and Security are present in `pom.xml` but unconfigured.
- **Environment**: No Docker setup for local development.

## Proposed Package Structure (Clean Architecture)
Based on `docs/architecture.md` and `README.md`:

```text
com.deliveryhub
├── domain/
│   ├── model/         # Pure Java Entities
│   ├── enums/         # Domain Enums (OrderStatus, UserRole)
│   └── exception/     # Domain Exceptions
├── application/
│   ├── usecase/       # Business Logic Orchestrators
│   ├── port/          # Input/Output Interfaces
│   │   ├── input/
│   │   └── output/
│   ├── dto/           # Data Transfer Objects
│   └── mapper/        # Manual Layer Mappers
├── presentation/
│   └── controller/    # REST/WS Controllers
└── infrastructure/
    ├── adapter/       # Implementations of output ports (Persistence, External APIs)
    ├── config/        # Spring Beans, Security, WebSocket config
    ├── entity/        # JPA Entities
    └── exception/     # Global Error Handling
```

## Configuration Plan

### application.yml
Convert `application.properties` to YAML and include:
- `spring.datasource`: PostgreSQL connection details (using environment variables for Docker).
- `spring.jpa`: Hibernate DDL-auto (none for Flyway), show-sql, etc.
- `spring.flyway`: Enabled for database migrations.
- `app.jwt`: Placeholders for secret and expiration.
- `server.port`: 8080.

### Docker Setup
- **Dockerfile**:
  - Stage 1: Maven build using `maven:3.9-eclipse-temurin-21`.
  - Stage 2: Runtime using `eclipse-temurin:21-jre-alpine`.
- **docker-compose.yml**:
  - `db`: PostgreSQL 17-alpine.
  - `pgadmin`: Administration tool for development.
  - `app` (Optional for Phase 1): The backend service itself.

## Risks and Mitigation
- **Package Relocation**: `Application.java` should stay in `com.deliveryhub` to automatically scan all sub-packages (`domain`, `application`, etc.).
- **Dependency Versions**: Spring Boot 4.0.6 is a future/experimental version in the `pom.xml`. Verification of standard configurations for this version is needed.
- **Database Availability**: Flyway will fail if the database is not ready; Docker Compose `depends_on` or retry logic in the app might be needed.

## Persisted Artifacts
- **Engram Topic**: `sdd/phase-1-foundations/explore`
- **OpenSpec File**: `openspec/changes/phase-1-foundations/explore.md`

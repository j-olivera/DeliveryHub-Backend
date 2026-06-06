# Design: Phase 1 Foundations

## Technical Approach
The foundation phase focuses on establishing a robust, containerized, and well-structured environment using **Clean Architecture**. This involves a source code restructuring, migration to YAML configuration, and the introduction of Docker orchestration for local development parity.

## Architecture Decisions

### 1. Clean Architecture Package Structure
- **Decision**: Organize the project into four distinct layers: `domain`, `application`, `presentation`, and `infrastructure`.
- **Rationale**: Decouples business logic from external frameworks, improving testability and maintainability.
- **Packages**:
  - `domain`: Contains pure Java entities, enums, and business exceptions.
  - `application`: Orchestrates business flow via Use Cases and defines Ports (interfaces).
  - `presentation`: Entry points (REST, WebSockets).
  - `infrastructure`: Technical implementations (JPA, Security, External APIs).

### 2. Configuration Management
- **Decision**: Migrate `application.properties` to `application.yml`.
- **Rationale**: YAML provides a hierarchical structure that is more readable for complex configurations and integrates seamlessly with Spring Boot's profile and environment variable systems.
- **Overrides**: Use `${DB_HOST:localhost}` style placeholders to allow zero-config local runs while being cloud-ready.

### 3. Containerization Strategy
- **Decision**: Implement a multi-stage `Dockerfile` and `docker-compose.yml`.
- **Rationale**: 
  - **Multi-stage Build**: Uses `maven:3.9-eclipse-temurin-21` for building and `eclipse-temurin:21-jre` for running, reducing the final image size and removing build tools from the production environment.
  - **Docker Compose**: Orchestrates `postgres:16-alpine` and `pgadmin4`. Uses `healthcheck` on the database to ensure the application waits for readiness.

## Data Flow
1. **Entry**: A request arrives at a `RestController` in the `presentation` layer.
2. **Transformation**: Data is mapped to a DTO and passed to an **Input Port** in the `application` layer.
3. **Orchestration**: A **Use Case** processes the request, interacting with the **Domain** entities.
4. **Persistence/External**: The Use Case calls an **Output Port** (interface), which is implemented by an adapter in the `infrastructure` layer.
5. **Response**: The result flows back through the layers, being mapped to a Response DTO for the client.

## File Changes

### Structural Changes
- **Source**: Move `Application.java` to `com.deliveryhub.Application`.
- **Packages**: Create `domain`, `application`, `presentation`, `infrastructure` sub-packages.

### Configuration Changes
- **New**: `src/main/resources/application.yml`
- **Delete**: `src/main/resources/application.properties`

### Dev-Ops Changes
- **New**: `Dockerfile` (root)
- **New**: `docker-compose.yml` (root)

## Interfaces / Contracts

### application.yml (Draft)
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:deliveryhub}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration

security:
  jwt:
    secret: ${JWT_SECRET:placeholder_secret_for_dev_only_32_chars_min}
    expiration: 3600000 # 1 hour
    issuer: deliveryhub-api
```

### Dockerfile (Draft)
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Testing Strategy
- **Layer Integrity**: Verify that `domain` classes have zero dependencies on Spring or Infrastructure.
- **Environment**: Validate that `docker-compose up` successfully initializes PostgreSQL and pgAdmin.
- **Connectivity**: Verify the Spring Boot application starts and connects to the Dockerized database.

## Migration / Rollout
1. **Prepare**: Ensure local Docker environment is ready.
2. **Refactor**: Perform `git mv` operations to restructure packages.
3. **Configure**: Add `application.yml` and verify local startup.
4. **Containerize**: Build and run the Docker image to ensure parity.

## Open Questions
- **Spring Boot 4.0.6**: Confirming if this version requires specific configuration deviations (handled as intentional per project specs).
- **Volume Persistence**: Ensure the `postgres_data` volume is correctly mapped to avoid data loss on container restart.

# Tasks: Phase 1 Foundations

## Review Workload Forecast
- **Estimated lines**: ~450
- **Delivery strategy**: `single-pr`
- **Decision needed before apply**: `No`
- **Chain strategy**: `size-exception`

## Phase 1: Cleanup
- [x] 1.1 Delete `src/main/resources/application.properties` to prepare for YAML migration.

## Phase 2: Configuration
- [x] 2.1 Create `src/main/resources/application.yml`.
- [x] 2.2 Configure PostgreSQL connection using environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).
- [x] 2.3 Enable Flyway migrations in `application.yml`.
- [x] 2.4 Add JWT configuration placeholders (`security.jwt.secret`, `security.jwt.expiration-ms`).
- [x] 2.5 Create `src/test/resources/application-test.yml` (H2) for TDD.

## Phase 3: Scaffolding
- [x] 3.1 Create `src/main/java/com/deliveryhub/domain/{model,enums,exception}`.
- [x] 3.2 Create `src/main/java/com/deliveryhub/application/{usecase,port/in,port/out,dto,mapper}`.
- [x] 3.3 Create `src/main/java/com/deliveryhub/infrastructure/{adapter,config,entity,exception}`.
- [x] 3.4 Create `src/main/java/com/deliveryhub/presentation/controller`.

## Phase 4: Dockerization
- [x] 4.1 Create multi-stage `Dockerfile` in the project root using JDK 21 for building and JRE 21 for running.
- [x] 4.2 Create `docker-compose.yml` with PostgreSQL 16, pgAdmin, and the application service.
- [x] 4.3 Configure healthchecks for the PostgreSQL service to ensure the application waits for it to be ready.

## Phase 5: Verification
- [x] 5.1 Run `./mvnw clean package` to verify the project builds correctly with the new structure.
- [x] 5.2 Run `docker-compose build && docker-compose up -d` to verify services start correctly.
- [x] 5.3 Check application logs to confirm successful connection to PostgreSQL and Flyway initialization.

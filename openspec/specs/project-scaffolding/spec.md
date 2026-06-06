# Project Scaffolding Specification

## Purpose
Define the Clean Architecture package structure for DeliveryHub to ensure separation of concerns and maintainability.

## Requirements

### Requirement: Package Structure
The application MUST be organized into four primary layers under `com.deliveryhub`: `domain`, `application`, `infrastructure`, and `presentation`.

#### Scenario: Layer Creation
GIVEN a new project initialization
WHEN the scaffolding phase is executed
THEN the following package structure MUST be present:
- `com.deliveryhub.domain`: `model`, `enums`, `exception`
- `com.deliveryhub.application`: `port.in`, `port.out`, `usecase`, `dto`, `mapper`
- `com.deliveryhub.infrastructure`: `persistence`, `security`, `websocket`, `mapper`
- `com.deliveryhub.presentation`: `rest`, `websocket`, `dto`

### Requirement: Dependency Rule
Dependencies SHALL only point inwards. The `domain` layer MUST NOT depend on any other layer. The `application` layer MUST NOT depend on `infrastructure` or `presentation`.

#### Scenario: Domain Isolation
GIVEN a class in the `domain` package
WHEN analyzing its imports
THEN it MUST NOT contain references to Spring Framework, JPA, or any external infrastructure library.

### Requirement: Application Entry Point
The main Spring Boot application class MUST be located at `com.deliveryhub.Application` to ensure default component scanning covers all sub-packages.

#### Scenario: Component Scanning
GIVEN the application is started
WHEN Spring Boot performs component scanning
THEN all layers (presentation, infrastructure, application) SHOULD be automatically discovered.

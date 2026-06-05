# Infraestructura — DeliveryHub

## Entorno local (Docker Compose)

Levantar todo el entorno con:
```bash
docker compose up -d
```

Servicios disponibles:
| Servicio   | URL                        | Credenciales                        |
|------------|----------------------------|-------------------------------------|
| API        | http://localhost:8080      | —                                   |
| PostgreSQL  | localhost:5432             | deliveryhub_user / deliveryhub_pass |
| pgAdmin    | http://localhost:5050      | admin@local.com / admin             |

## Perfiles de Spring

| Perfil | Cuándo se usa          | Base de datos         |
|--------|------------------------|-----------------------|
| `dev`  | Desarrollo local       | Docker PostgreSQL local|
| `test` | Tests de integración   | Testcontainers         |
| `prod` | Despliegue             | Variable de entorno    |

Activar perfil: `SPRING_PROFILES_ACTIVE=dev`

## Variables de entorno (producción)

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/deliveryhub_db
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION_MS=86400000
CORS_ALLOWED_ORIGINS=https://tudominio.com
```

Nunca hardcodear estas variables. Nunca commitearlas. Usar `.env` local (en `.gitignore`) y secrets del CI/CD en producción.

## CI/CD — GitHub Actions

### Pipeline CI (`.github/workflows/ci.yml`)
Se ejecuta en cada push y pull request a `develop` y `main`.

```yaml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
      - name: Compilar y ejecutar tests
        run: mvn verify
      - name: Publicar reporte de cobertura
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: target/site/jacoco/

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      - name: Instalar dependencias
        run: npm ci
        working-directory: frontend
      - name: Build
        run: npm run build -- --configuration production
        working-directory: frontend
      - name: Tests
        run: npm test -- --watch=false --browsers=ChromeHeadless
        working-directory: frontend
```

### Pipeline CD (`.github/workflows/cd.yml`)
Se ejecuta solo en merge a `main`.

```yaml
name: CD

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Build imagen Docker backend
        run: docker build -t ghcr.io/${{ github.repository }}/backend:latest ./backend

      - name: Push al registry
        run: |
          echo ${{ secrets.GITHUB_TOKEN }} | docker login ghcr.io -u ${{ github.actor }} --password-stdin
          docker push ghcr.io/${{ github.repository }}/backend:latest

      - name: Deploy en Render / Railway
        run: curl -X POST ${{ secrets.DEPLOY_WEBHOOK_URL }}
```

## Dockerfile backend (referencia)

```dockerfile
# Build stage
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Runtime stage
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Reglas

- Nunca commitear archivos `.env` o cualquier secret.
- Los tests deben pasar en CI antes de mergear a `main`.
- No hacer deploy manual: todo pasa por el pipeline CD.
- Las imágenes Docker se tagean con el SHA del commit además de `latest`.
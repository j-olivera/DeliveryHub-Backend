# Technical Design: User Login Security

Este documento describe la especificación técnica para implementar la autenticación basada en JWT, hashing de contraseñas con BCrypt y la configuración de seguridad en el backend de DeliveryHub.

---

## 1. Arquitectura de Seguridad

La arquitectura de seguridad sigue los principios de **Clean Architecture** definidos en [AGENTS.md](file:///home/j-olivera/Projects/DeliveryHub-Backend/AGENTS.md). 

```
Presentation (AuthController) 
    ↓
Application (LoginUseCase) 
    ↓
Domain (User / Password Hashing Policy)
    ↑
Infrastructure (SecurityConfig, JwtFilter, UserDetailsServiceImpl)
```

Las dependencias de Spring Security y JWT quedan encapsuladas en la capa de **Infrastructure** y **Presentation**, manteniendo la capa de **Domain** y **Application** libres de dependencias del framework de seguridad.

---

## 2. Componentes a Implementar o Modificar

A continuación se detallan las clases, interfaces y componentes requeridos en cada capa de la arquitectura:

| Capa | Componente | Tipo | Descripción |
| :--- | :--- | :--- | :--- |
| **Domain** | [User](file:///home/j-olivera/Projects/DeliveryHub-Backend/src/main/java/com/deliveryhub/domain/model/User.java) | Clase (Modificado) | Mantendrá contraseñas hasheadas en base de datos. Se valida el flujo de creación. |
| **Application** | `LoginUseCase` | Interfaz (Nuevo) | Puerto de entrada (`application.port.in`) para autenticar credenciales. |
| **Application** | `LoginUseCaseImpl` | Clase (Nuevo) | Caso de uso (`application.usecase`) que valida credenciales y delega la generación de token. |
| **Application** | `LoginResponse` | DTO (Nuevo) | Record que contiene el token JWT, `userId` y `role` (`application.dto`). |
| **Application** | `RegisterUserUseCaseImpl` | Clase (Modificado) | Se inyectará un port o componente inmutable de cifrado para codificar la contraseña antes de crear el `User`. |
| **Infrastructure**| `SecurityConfig` | Clase (Nuevo) | Configuración de Spring Security (`infrastructure.security`). Habilita stateless session, desactiva CSRF, define accesos por rol y registra beans de codificación. |
| **Infrastructure**| `JwtUtils` | Clase (Nuevo) | Utilidades para generar, decodificar y validar tokens firmados con HMAC-SHA256 (`infrastructure.security`). |
| **Infrastructure**| `JwtFilter` | Clase (Nuevo) | Filtro de servlet HTTP (`OncePerRequestFilter`) que procesa cabeceras `Authorization` e inicializa el contexto de seguridad. |
| **Infrastructure**| `UserDetailsServiceImpl` | Clase (Nuevo) | Implementación de `UserDetailsService` de Spring Security que mapea el dominio a `UserDetails`. |
| **Presentation**  | `AuthController` | Clase (Nuevo) | Controlador REST (`presentation.rest`) expuesto en `POST /api/v1/auth/login`. |

---

## 3. Especificación Técnica de Clases Clave

### 3.1. Hashing de Contraseñas (Registro)
Para evitar dependencias directas de Spring Security en `RegisterUserUseCaseImpl`, se creará un puerto de salida simple en la capa de aplicación:

```java
package com.deliveryhub.application.port.out;

public interface PasswordEncoderPort {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
```

La implementación en infraestructura inyectará `BCryptPasswordEncoder`:

```java
package com.deliveryhub.infrastructure.security;

import com.deliveryhub.application.port.out.PasswordEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderAdapter implements PasswordEncoderPort {
    private final PasswordEncoder passwordEncoder;
    public PasswordEncoderAdapter(PasswordEncoder passwordEncoder) { this.passwordEncoder = passwordEncoder; }
    @Override public String encode(CharSequence rawPassword) { return passwordEncoder.encode(rawPassword); }
    @Override public boolean matches(CharSequence rawPassword, String encodedPassword) { return passwordEncoder.matches(rawPassword, encodedPassword); }
}
```

### 3.2. Configuración de Seguridad (`SecurityConfig`)
- **Sesión**: `SessionCreationPolicy.STATELESS`
- **Filtros**: Registrar `JwtFilter` antes de `UsernamePasswordAuthenticationFilter`.
- **Reglas de Acceso**:
  - `POST /api/v1/auth/login` y `POST /api/v1/users` (registro público) -> `permitAll()`
  - Rutas bajo `/api/v1/admin/**` -> Requiere rol `ADMIN`
  - Rutas bajo `/api/v1/delivery/**` -> Requiere rol `DELIVERY` (o `ADMIN` donde aplique)
  - El resto de endpoints (`/api/v1/**`) -> `authenticated()`

### 3.3. Estructura de JWT (`JwtUtils`)
- **Algoritmo**: HMAC256 usando `keys.secret` configurado en `application.yml`.
- **Expiración**: Por defecto 24 horas (`86400` segundos).
- **Claims**: El token contendrá los claims `userId` y `role`.

---

## 4. Estrategia de Testing

El plan de pruebas sigue la pirámide de testing de [docs/testing.md](file:///home/j-olivera/Projects/DeliveryHub-Backend/AGENTS.md):

1. **Pruebas Unitarias** (`LoginUseCaseImplTest` y `RegisterUserUseCaseImplTest`):
   - Verificar flujo de autenticación exitoso y fallido (lanzar `InvalidCredentialsException` u homóloga).
   - Mockear puertos (`UserRepositoryPort` y `PasswordEncoderPort`).
2. **Pruebas de Integración Web** (`AuthControllerIntegrationTest` con `@WebMvcTest` y `@Import(SecurityConfig.class)`):
   - Validar endpoint `POST /api/v1/auth/login` con credenciales válidas e inválidas.
   - Validar comportamiento del middleware de seguridad (401 Unauthorized en endpoints protegidos, 403 Forbidden cuando el rol no coincide).
3. **Pruebas de Filtro de Seguridad**:
   - Comprobar que peticiones con tokens válidos (e.g. `Authorization: Bearer <token>`) propaguen el contexto de autenticación de Spring Security adecuadamente.

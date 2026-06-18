# Design: Implement User Persistence

## Technical Approach

We will implement database persistence for the `User` aggregate root following Clean Architecture. The Domain layer exposes `UserRepositoryPort`. We will implement `UserRepositoryAdapter` in the Infrastructure layer using Spring Data JPA (`UserJpaRepository`) and standard JPA entities (`UserEntity`). The schema will be managed via a Flyway migration.

## Architecture Decisions

| Decision | Options | Tradeoffs | Choice |
|---|---|---|---|
| **UserRole Mapping** | JPA EnumType.STRING vs EnumType.ORDINAL | ORDINAL is brittle if enums are reordered; STRING is self-documenting and matches Postgres CHECK constraint. | `EnumType.STRING` |
| **ID Generation** | JPA `@GeneratedValue` vs Domain Assigned UUID | Domain-assigned UUID ensures that the aggregate root always has an identity in memory. JPA `@GeneratedValue` can cause detached state sync issues. | Domain-assigned (no `@GeneratedValue` annotation, ID passed from domain model) |
| **Mapper Pattern** | MapStruct vs Manual Mapper | MapStruct automates mapping but adds build-time annotation processing. Manual mapping is simple, explicit, and matches existing project class conventions. | Manual Mapper as a `@Component` class |

## Data Flow

```text
RegisterUserUseCase ──> UserRepositoryPort (Interface)
                             ▲
                             │ (implements)
                    UserRepositoryAdapter
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
       UserMapper                      UserJpaRepository
  (User <─> UserEntity)                       │
                                              ▼
                                          PostgreSQL
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/resources/db/migration/V1__create_users_table.sql` | Create | Flyway schema definition for `users` table. |
| `src/main/java/com/deliveryhub/infrastructure/persistence/entity/UserEntity.java` | Create | JPA Entity for users mapping to db columns. |
| `src/main/java/com/deliveryhub/infrastructure/persistence/UserJpaRepository.java` | Create | Spring Data JPA Repository interface. |
| `src/main/java/com/deliveryhub/infrastructure/persistence/UserRepositoryAdapter.java` | Create | Repository Adapter implementing `UserRepositoryPort`. |
| `src/main/java/com/deliveryhub/infrastructure/mapper/UserMapper.java` | Create | Mapper component converting between `User` and `UserEntity`. |
| `src/test/java/com/deliveryhub/infrastructure/persistence/UserRepositoryAdapterTest.java` | Create | Integration tests for adapter using `@DataJpaTest`. |

## Interfaces / Contracts

### UserEntity.java
```java
package com.deliveryhub.infrastructure.persistence.entity;

import com.deliveryhub.domain.enums.UserRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters, Setters, Constructors
}
```

### UserMapper.java
```java
package com.deliveryhub.infrastructure.mapper;

import com.deliveryhub.domain.model.User;
import com.deliveryhub.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return User.reconstruct(
            entity.getId(),
            entity.getName(),
            entity.getEmail(),
            entity.getPassword(),
            entity.getRole()
        );
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) return null;
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setRole(domain.getRole());
        return entity;
    }
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Integration | Save & Retrieve | `@DataJpaTest` verification with H2 in compatibility mode, validating mappings, null constraints, and default values. |
| Integration | Email/Name Existence Checks | Verification of `existsByEmail` and `existsByName` logic. |

## Migration / Rollout

A Flyway script `V1__create_users_table.sql` will execute automatically on application startup.
No data migration required as this is a new table.

## Open Questions

- None.

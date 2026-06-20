package com.deliveryhub.infrastructure.persistence;

import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.infrastructure.mapper.UserMapper;
import com.deliveryhub.infrastructure.entity.user.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.deliveryhub.infrastructure.adapter.UserRepositoryAdapter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({UserRepositoryAdapter.class, UserMapper.class})
class UserRepositoryAdapterTest {

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    void saveAndRetrieveUser_whenValidUser_shouldPersistAndReturnCorrectData() {
        // Arrange
        User user = User.create("Test User", "test@deliveryhub.com", "securePassword123", UserRole.DELIVERY);

        // Act
        User savedUser = userRepositoryAdapter.save(user);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isEqualTo(user.getId());
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getEmail()).isEqualTo("test@deliveryhub.com");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.DELIVERY);

        // Verify retrieval from DB
        Optional<UserEntity> entityOpt = userJpaRepository.findById(user.getId());
        assertThat(entityOpt).isPresent();
        UserEntity entity = entityOpt.get();
        assertThat(entity.getName()).isEqualTo("Test User");
        assertThat(entity.getEmail()).isEqualTo("test@deliveryhub.com");
        assertThat(entity.getPassword()).isEqualTo("securePassword123");
        assertThat(entity.getRole()).isEqualTo(UserRole.DELIVERY);
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void existsByEmail_whenEmailExists_shouldReturnTrue() {
        // Arrange
        User user = User.create("Unique Name 1", "exists@deliveryhub.com", "pwd", UserRole.DELIVERY);
        userRepositoryAdapter.save(user);

        // Act
        boolean exists = userRepositoryAdapter.existsByEmail("exists@deliveryhub.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_whenEmailDoesNotExist_shouldReturnFalse() {
        // Act
        boolean exists = userRepositoryAdapter.existsByEmail("nonexistent@deliveryhub.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void existsByName_whenNameExists_shouldReturnTrue() {
        // Arrange
        User user = User.create("Exists Name", "existsname@deliveryhub.com", "pwd", UserRole.DELIVERY);
        userRepositoryAdapter.save(user);

        // Act
        boolean exists = userRepositoryAdapter.existsByName("Exists Name");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_whenNameDoesNotExist_shouldReturnFalse() {
        // Act
        boolean exists = userRepositoryAdapter.existsByName("Nonexistent Name");

        // Assert
        assertThat(exists).isFalse();
    }
}

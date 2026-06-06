package com.deliveryhub.domain.model;

import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.exception.DomainValidationException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * User aggregate root representing a user in the delivery system.
 */
public class User {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final UUID id;
    private final String name;
    private final String email;
    private final String password;
    private final UserRole role;

    private User(UUID id, String name, String email, String password, UserRole role) {
        validateMandatoryFields(name, email, role);
        validateEmailFormat(email);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    private void validateMandatoryFields(String name, String email, UserRole role) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Name cannot be blank");
        }
        if (email == null || email.isBlank()) {
            throw new DomainValidationException("Email cannot be blank");
        }
        if (role == null) {
            throw new DomainValidationException("Role is mandatory");
        }
    }

    private void validateEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new DomainValidationException("Invalid email format");
        }
    }

    /**
     * Factory method to create a new User with a random ID.
     *
     * @param name     the user's name
     * @param email    the user's email
     * @param password the user's password
     * @param role     the user's role
     * @return a new User instance
     */
    public static User create(String name, String email, String password, UserRole role) {
        return new User(UUID.randomUUID(), name, email, password, role);
    }

    /**
     * Factory method to reconstruct a User from an existing state.
     *
     * @param id       the user's ID
     * @param name     the user's name
     * @param email    the user's email
     * @param password the user's password
     * @param role     the user's role
     * @return a reconstructed User instance
     */
    public static User reconstruct(UUID id, String name, String email, String password, UserRole role) {
        return new User(id, name, email, password, role);
    }

    /**
     * Gets the user's ID.
     *
     * @return the user's ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the user's name.
     *
     * @return the user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the user's email.
     *
     * @return the user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the user's password.
     *
     * @return the user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the user's role.
     *
     * @return the user's role
     */
    public UserRole getRole() {
        return role;
    }
}

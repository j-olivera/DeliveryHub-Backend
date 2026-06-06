package com.deliveryhub.domain.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserRoleTest {
    @Test
    void shouldHaveExpectedRoles() {
        assertEquals(2, UserRole.values().length);
        assertNotNull(UserRole.valueOf("ADMIN"));
        assertNotNull(UserRole.valueOf("DELIVERY"));
    }
}

package com.deliveryhub.domain.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {
    @Test
    void shouldHaveExpectedStatuses() {
        assertEquals(5, OrderStatus.values().length);
        assertNotNull(OrderStatus.valueOf("PENDING"));
        assertNotNull(OrderStatus.valueOf("ASSIGNED"));
        assertNotNull(OrderStatus.valueOf("IN_PROGRESS"));
        assertNotNull(OrderStatus.valueOf("DELIVERED"));
        assertNotNull(OrderStatus.valueOf("CANCELLED"));
    }
}

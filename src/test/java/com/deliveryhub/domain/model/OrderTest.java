package com.deliveryhub.domain.model;

import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.exception.InvalidStatusTransitionException;
import com.deliveryhub.domain.exception.UnauthorizedTransitionException;
import com.deliveryhub.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private final Address dummyAddress = new Address("Main St 123", "Springfield", 40.7128, -74.0060);
    private final UUID adminId = UUID.randomUUID();

    @Test
    @DisplayName("Should create a new order in PENDING status")
    void shouldCreateNewOrderInPendingStatus() {
        Order order = Order.create(adminId, "Alice", "+5491112345678", dummyAddress, "Timbre roto");
        
        assertNotNull(order.getId());
        assertEquals(adminId, order.getAdminUserId());
        assertEquals("Alice", order.getCustomerName());
        assertEquals("+5491112345678", order.getCustomerPhone());
        assertEquals(dummyAddress, order.getCustomerAddress());
        assertEquals("Timbre roto", order.getNotes());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNull(order.getDeliveryUserId());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertEquals(order.getCreatedAt(), order.getUpdatedAt());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when creating order with null adminUserId")
    void shouldThrowDomainValidationExceptionWhenCreatingOrderWithNullAdminUserId() {
        assertThrows(DomainValidationException.class, () -> 
            Order.create(null, "Alice", "+5491112345678", dummyAddress, "Notes")
        );
    }

    @Test
    @DisplayName("Should throw DomainValidationException when creating order with blank customerName")
    void shouldThrowDomainValidationExceptionWhenCreatingOrderWithBlankCustomerName() {
        assertThrows(DomainValidationException.class, () -> 
            Order.create(adminId, "", "+5491112345678", dummyAddress, "Notes")
        );
        assertThrows(DomainValidationException.class, () -> 
            Order.create(adminId, null, "+5491112345678", dummyAddress, "Notes")
        );
    }

    @Test
    @DisplayName("Should throw DomainValidationException when creating order with null customerAddress")
    void shouldThrowDomainValidationExceptionWhenCreatingOrderWithNullCustomerAddress() {
        assertThrows(DomainValidationException.class, () -> 
            Order.create(adminId, "Alice", "+5491112345678", null, "Notes")
        );
    }

    @Test
    @DisplayName("Should reconstruct an existing order")
    void shouldReconstructExistingOrder() {
        UUID id = UUID.randomUUID();
        UUID deliveryUserId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime assignedAt = LocalDateTime.now().minusHours(5);
        LocalDateTime deliveredAt = LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        OrderStatus status = OrderStatus.DELIVERED;

        Order order = Order.reconstruct(id, adminId, "Alice", "+5491112345678", dummyAddress, "Notes", status, 
                                        deliveryUserId, createdAt, assignedAt, deliveredAt, updatedAt);

        assertEquals(id, order.getId());
        assertEquals(adminId, order.getAdminUserId());
        assertEquals("Alice", order.getCustomerName());
        assertEquals("+5491112345678", order.getCustomerPhone());
        assertEquals(dummyAddress, order.getCustomerAddress());
        assertEquals("Notes", order.getNotes());
        assertEquals(status, order.getStatus());
        assertEquals(deliveryUserId, order.getDeliveryUserId());
        assertEquals(createdAt, order.getCreatedAt());
        assertEquals(assignedAt, order.getAssignedAt());
        assertEquals(deliveredAt, order.getDeliveredAt());
        assertEquals(updatedAt, order.getUpdatedAt());
    }

    @Test
    @DisplayName("Should assign a delivery user to a PENDING order when requested by ADMIN")
    void shouldAssignDeliveryUserToPendingOrderWhenRequestedByAdmin() {
        Order order = Order.create(adminId, "Alice", "+5491112345678", dummyAddress, "Notes");
        UUID deliveryUserId = UUID.randomUUID();

        order.assign(deliveryUserId, UserRole.ADMIN);

        assertEquals(OrderStatus.ASSIGNED, order.getStatus());
        assertEquals(deliveryUserId, order.getDeliveryUserId());
        assertNotNull(order.getAssignedAt());
        assertTrue(order.getUpdatedAt().isAfter(order.getCreatedAt()) || order.getUpdatedAt().equals(order.getCreatedAt()));
    }

    @Test
    @DisplayName("Should throw UnauthorizedTransitionException when assigning by non-ADMIN user")
    void shouldThrowUnauthorizedTransitionExceptionWhenAssigningByNonAdminUser() {
        Order order = Order.create(adminId, "Alice", "+5491112345678", dummyAddress, "Notes");
        UUID deliveryUserId = UUID.randomUUID();

        assertThrows(UnauthorizedTransitionException.class, () -> order.assign(deliveryUserId, UserRole.DELIVERY));
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when assigning a delivery user to a non-PENDING order")
    void shouldThrowInvalidStatusTransitionExceptionWhenAssigningToNonPendingOrder() {
        Order order = Order.reconstruct(
            UUID.randomUUID(), adminId, "Alice", "+5491112345678", dummyAddress, "Notes",
            OrderStatus.ASSIGNED, UUID.randomUUID(),
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now()
        );

        assertThrows(InvalidStatusTransitionException.class, () -> order.assign(UUID.randomUUID(), UserRole.ADMIN));
    }

    @Test
    @DisplayName("Should throw DomainValidationException when assigning null deliveryUserId")
    void shouldThrowDomainValidationExceptionWhenAssigningNullDeliveryUserId() {
        Order order = Order.create(adminId, "Alice", "+5491112345678", dummyAddress, "Notes");
        assertThrows(DomainValidationException.class, () -> order.assign(null, UserRole.ADMIN));
    }

    @Test
    @DisplayName("Should start delivery of an ASSIGNED order by authorized delivery user")
    void shouldStartDeliveryOfAssignedOrder() {
        UUID deliveryUserId = UUID.randomUUID();
        Order order = Order.reconstruct(
            UUID.randomUUID(), adminId, "Alice", "+5491112345678", dummyAddress, "Notes",
            OrderStatus.ASSIGNED, deliveryUserId,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now()
        );

        order.startDelivery(deliveryUserId, UserRole.DELIVERY);

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertTrue(order.getUpdatedAt().isAfter(order.getCreatedAt()));
    }

    @Test
    @DisplayName("Should throw UnauthorizedTransitionException when starting delivery by ADMIN")
    void shouldThrowUnauthorizedTransitionExceptionWhenStartingDeliveryByAdmin() {
        UUID deliveryUserId = UUID.randomUUID();
        Order order = Order.reconstruct(
            UUID.randomUUID(), adminId, "Alice", "+5491112345678", dummyAddress, "Notes",
            OrderStatus.ASSIGNED, deliveryUserId,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now()
        );

        assertThrows(UnauthorizedTransitionException.class, () -> order.startDelivery(deliveryUserId, UserRole.ADMIN));
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when starting delivery from non-ASSIGNED order")
    void shouldThrowInvalidStatusTransitionExceptionWhenStartingDeliveryFromNonAssignedOrder() {
        Order order = Order.create(adminId, "Alice", "+5491112345678", dummyAddress, "Notes");
        assertThrows(InvalidStatusTransitionException.class, () -> order.startDelivery(UUID.randomUUID(), UserRole.DELIVERY));
    }

    @Test
    @DisplayName("Should throw UnauthorizedTransitionException when starting delivery by unassigned delivery user")
    void shouldThrowUnauthorizedTransitionExceptionWhenStartingDeliveryByUnassignedUser() {
        UUID deliveryUserId = UUID.randomUUID();
        Order order = Order.reconstruct(
            UUID.randomUUID(), adminId, "Alice", "+5491112345678", dummyAddress, "Notes",
            OrderStatus.ASSIGNED, deliveryUserId,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now()
        );

        assertThrows(UnauthorizedTransitionException.class, () -> order.startDelivery(UUID.randomUUID(), UserRole.DELIVERY));
    }

    @Test
    @DisplayName("Should complete delivery of an IN_PROGRESS order by authorized delivery user")
    void shouldCompleteDeliveryOfInProgressOrder() {
        UUID deliveryUserId = UUID.randomUUID();
        Order order = Order.reconstruct(
            UUID.randomUUID(), adminId, "Alice", "+5491112345678", dummyAddress, "Notes",
            OrderStatus.IN_PROGRESS, deliveryUserId,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now()
        );

        order.completeDelivery(deliveryUserId, UserRole.DELIVERY);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertNotNull(order.getDeliveredAt());
        assertTrue(order.getUpdatedAt().isAfter(order.getCreatedAt()));
    }

    @Test
    @DisplayName("Should throw UnauthorizedTransitionException when completing delivery by ADMIN")
    void shouldThrowUnauthorizedTransitionExceptionWhenCompletingDeliveryByAdmin() {
        UUID deliveryUserId = UUID.randomUUID();
        Order order = Order.reconstruct(
            UUID.randomUUID(), adminId, "Alice", "+5491112345678", dummyAddress, "Notes",
            OrderStatus.IN_PROGRESS, deliveryUserId,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now()
        );

        assertThrows(UnauthorizedTransitionException.class, () -> order.completeDelivery(deliveryUserId, UserRole.ADMIN));
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when completing delivery from non-IN_PROGRESS order")
    void shouldThrowInvalidStatusTransitionExceptionWhenCompletingDeliveryFromNonInProgressOrder() {
        Order order = Order.create(adminId, "Alice", "+5491112345678", dummyAddress, "Notes");
        assertThrows(InvalidStatusTransitionException.class, () -> order.completeDelivery(UUID.randomUUID(), UserRole.DELIVERY));
    }

    @Test
    @DisplayName("Should throw UnauthorizedTransitionException when completing delivery by unassigned delivery user")
    void shouldThrowUnauthorizedTransitionExceptionWhenCompletingDeliveryByUnassignedUser() {
        UUID deliveryUserId = UUID.randomUUID();
        Order order = Order.reconstruct(
            UUID.randomUUID(), adminId, "Alice", "+5491112345678", dummyAddress, "Notes",
            OrderStatus.IN_PROGRESS, deliveryUserId,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now()
        );

        assertThrows(UnauthorizedTransitionException.class, () -> order.completeDelivery(UUID.randomUUID(), UserRole.DELIVERY));
    }

    @Test
    @DisplayName("Should cancel an order when requested by ADMIN and not in terminal state")
    void shouldCancelOrderWhenRequestedByAdminAndNotInTerminalState() {
        Order order = Order.create(adminId, "Alice", "+5491112345678", dummyAddress, "Notes");
        
        order.cancel(UserRole.ADMIN);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertTrue(order.getUpdatedAt().isAfter(order.getCreatedAt()) || order.getUpdatedAt().equals(order.getCreatedAt()));
    }

    @Test
    @DisplayName("Should throw UnauthorizedTransitionException when cancelling by non-ADMIN user")
    void shouldThrowUnauthorizedTransitionExceptionWhenCancellingByNonAdminUser() {
        Order order = Order.create(adminId, "Alice", "+5491112345678", dummyAddress, "Notes");
        
        assertThrows(UnauthorizedTransitionException.class, () -> order.cancel(UserRole.DELIVERY));
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when cancelling a terminal DELIVERED order")
    void shouldThrowInvalidStatusTransitionExceptionWhenCancellingDeliveredOrder() {
        Order order = Order.reconstruct(
            UUID.randomUUID(), adminId, "Alice", "+5491112345678", dummyAddress, "Notes",
            OrderStatus.DELIVERED, UUID.randomUUID(),
            LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        assertThrows(InvalidStatusTransitionException.class, () -> order.cancel(UserRole.ADMIN));
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when cancelling an already CANCELLED order")
    void shouldThrowInvalidStatusTransitionExceptionWhenCancellingCancelledOrder() {
        Order order = Order.reconstruct(
            UUID.randomUUID(), adminId, "Alice", "+5491112345678", dummyAddress, "Notes",
            OrderStatus.CANCELLED, null,
            LocalDateTime.now(), null, null, LocalDateTime.now()
        );

        assertThrows(InvalidStatusTransitionException.class, () -> order.cancel(UserRole.ADMIN));
    }
}

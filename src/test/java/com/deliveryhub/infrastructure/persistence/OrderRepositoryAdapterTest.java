package com.deliveryhub.infrastructure.persistence;

import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import com.deliveryhub.infrastructure.adapter.OrderRepositoryAdapter;
import com.deliveryhub.infrastructure.entity.order.OrderEntity;
import com.deliveryhub.infrastructure.entity.user.UserEntity;
import com.deliveryhub.infrastructure.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integración para la persistencia de las órdenes.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({OrderRepositoryAdapter.class, OrderMapper.class})
class OrderRepositoryAdapterTest {

    @Autowired
    private OrderRepositoryAdapter orderRepositoryAdapter;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private UUID adminId;
    private UUID deliveryId;

    @BeforeEach
    void setUp() {
        // Guardar un usuario administrador ficticio para poder referenciarlo como clave foránea
        UserEntity admin = new UserEntity();
        adminId = UUID.randomUUID();
        admin.setId(adminId);
        admin.setName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setPassword("pwd");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        userJpaRepository.save(admin);

        // Guardar un usuario repartidor ficticio para referenciarlo
        UserEntity delivery = new UserEntity();
        deliveryId = UUID.randomUUID();
        delivery.setId(deliveryId);
        delivery.setName("Delivery User");
        delivery.setEmail("delivery@test.com");
        delivery.setPassword("pwd");
        delivery.setRole(UserRole.DELIVERY);
        delivery.setActive(true);
        userJpaRepository.save(delivery);
    }

    @Test
    void saveAndRetrieveOrder_shouldPersistAndReturnCorrectData() {
        // Arrange
        Address address = new Address("Av. Rivadavia 1234", "Buenos Aires", -34.6037, -58.3816);
        Order order = Order.create(adminId, "Juan", "+5491112345678", address, "Timbre roto");

        // Act
        Order savedOrder = orderRepositoryAdapter.save(order);

        // Assert
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isEqualTo(order.getId());
        assertThat(savedOrder.getCustomerName()).isEqualTo("Juan");
        assertThat(savedOrder.getCustomerPhone()).isEqualTo("+5491112345678");
        assertThat(savedOrder.getNotes()).isEqualTo("Timbre roto");
        assertThat(savedOrder.getCustomerAddress().street()).isEqualTo("Av. Rivadavia 1234");
        assertThat(savedOrder.getCustomerAddress().city()).isEqualTo("Buenos Aires");

        // Verificar la recuperación desde la base de datos
        Optional<OrderEntity> entityOpt = orderJpaRepository.findById(order.getId());
        assertThat(entityOpt).isPresent();
        OrderEntity entity = entityOpt.get();
        assertThat(entity.getCustomerName()).isEqualTo("Juan");
        assertThat(entity.getAdminUserId()).isEqualTo(adminId);
        assertThat(entity.getAddress()).isNotNull();
        assertThat(entity.getAddress().getStreet()).isEqualTo("Av. Rivadavia 1234");
    }

    @Test
    void findAllByDeliveryUserId_shouldReturnOnlyAssignedOrders() {
        // Arrange
        Address address = new Address("Av. Rivadavia 1234", "Buenos Aires", -34.6037, -58.3816);
        Order order1 = Order.create(adminId, "Juan", "+5491112345678", address, "Timbre roto");
        order1.assign(deliveryId, UserRole.ADMIN);
        orderRepositoryAdapter.save(order1);

        Order order2 = Order.create(adminId, "Pedro", "+5491112345678", address, "Notes");
        orderRepositoryAdapter.save(order2); // Pedido no asignado

        // Act
        Page<Order> result = orderRepositoryAdapter.findAllByDeliveryUserId(deliveryId, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(order1.getId());
    }

    @Test
    void findAllByStatus_shouldReturnOnlyMatchingOrders() {
        // Arrange
        Address address = new Address("Av. Rivadavia 1234", "Buenos Aires", -34.6037, -58.3816);
        Order order1 = Order.create(adminId, "Juan", "+5491112345678", address, "Timbre roto");
        orderRepositoryAdapter.save(order1);

        Order order2 = Order.create(adminId, "Pedro", "+5491112345678", address, "Notes");
        order2.assign(deliveryId, UserRole.ADMIN);
        orderRepositoryAdapter.save(order2);

        // Act
        Page<Order> result = orderRepositoryAdapter.findAllByStatus(OrderStatus.PENDING, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(order1.getId());
    }

    @Test
    void hasActiveOrders_whenUserHasAssignedOrInProgressOrders_shouldReturnTrue() {
        // Arrange
        Address address = new Address("Av. Rivadavia 1234", "Buenos Aires", -34.6037, -58.3816);
        Order order = Order.create(adminId, "Juan", "+5491112345678", address, "Notes");
        order.assign(deliveryId, UserRole.ADMIN);
        orderRepositoryAdapter.save(order);

        // Act
        boolean hasActive = orderRepositoryAdapter.hasActiveOrders(deliveryId);

        // Assert
        assertThat(hasActive).isTrue();
    }

    @Test
    void hasActiveOrders_whenUserHasNoActiveOrders_shouldReturnFalse() {
        // Act
        boolean hasActive = orderRepositoryAdapter.hasActiveOrders(deliveryId);

        // Assert
        assertThat(hasActive).isFalse();
    }
}

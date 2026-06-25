package com.deliveryhub.application.usecase.order;

import com.deliveryhub.application.port.in.order.AssignOrderUseCase;
import com.deliveryhub.application.port.out.order.OrderRepositoryPort;
import com.deliveryhub.application.port.out.user.UserRepositoryPort;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.Order;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.domain.exception.DomainValidationException;
import com.deliveryhub.domain.exception.OrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del caso de uso AssignOrderUseCase.
 */
@Service
@Transactional
public class AssignOrderUseCaseImpl implements AssignOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final UserRepositoryPort userRepository;

    /**
     * Constructor del caso de uso.
     *
     * @param orderRepository puerto de persistencia de pedidos
     * @param userRepository  puerto de persistencia de usuarios
     */
    public AssignOrderUseCaseImpl(OrderRepositoryPort orderRepository, UserRepositoryPort userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Order execute(UUID orderId, UUID deliveryUserId, UUID adminUserId) {
        // Buscar el pedido
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Buscar el repartidor
        User deliveryUser = userRepository.findById(deliveryUserId)
            .orElseThrow(() -> new DomainValidationException("Delivery user not found"));

        // Validar que el usuario tenga el rol de DELIVERY
        if (deliveryUser.getRole() != UserRole.DELIVERY) {
            throw new DomainValidationException("Assigned user must have DELIVERY role");
        }

        // Validar que el repartidor esté activo
        if (!deliveryUser.isActive()) {
            throw new DomainValidationException("Delivery user is not active");
        }

        // Validar que el repartidor no tenga pedidos activos (ASSIGNED o IN_PROGRESS)
        if (orderRepository.hasActiveOrders(deliveryUserId)) {
            throw new DomainValidationException("Delivery user already has active orders");
        }

        // Realizar la asignación a través de la lógica de dominio
        order.assign(deliveryUserId, UserRole.ADMIN);

        return orderRepository.save(order);
    }
}

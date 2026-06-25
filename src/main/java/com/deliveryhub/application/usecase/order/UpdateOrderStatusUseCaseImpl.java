package com.deliveryhub.application.usecase.order;

import com.deliveryhub.application.port.in.order.UpdateOrderStatusUseCase;
import com.deliveryhub.application.port.out.order.OrderRepositoryPort;
import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.Order;
import com.deliveryhub.domain.exception.OrderNotFoundException;
import com.deliveryhub.domain.exception.UnauthorizedTransitionException;
import com.deliveryhub.domain.exception.InvalidStatusTransitionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del caso de uso UpdateOrderStatusUseCase.
 */
@Service
@Transactional
public class UpdateOrderStatusUseCaseImpl implements UpdateOrderStatusUseCase {

    private final OrderRepositoryPort orderRepository;

    /**
     * Constructor del caso de uso.
     *
     * @param orderRepository puerto de persistencia de pedidos
     */
    public UpdateOrderStatusUseCaseImpl(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order execute(UUID orderId, OrderStatus newStatus, UUID requesterId, String requesterRoleName) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        UserRole requesterRole = UserRole.valueOf(requesterRoleName);

        // Delegar lógica de transición de estados al modelo de dominio Order según el rol del solicitante
        if (requesterRole == UserRole.DELIVERY) {
            if (newStatus == OrderStatus.IN_PROGRESS) {
                order.startDelivery(requesterId, requesterRole);
            } else if (newStatus == OrderStatus.DELIVERED) {
                order.completeDelivery(requesterId, requesterRole);
            } else {
                throw new InvalidStatusTransitionException(
                    String.format("Delivery user cannot transition order to status %s", newStatus)
                );
            }
        } else if (requesterRole == UserRole.ADMIN) {
            if (newStatus == OrderStatus.CANCELLED) {
                order.cancel(requesterRole);
            } else {
                throw new InvalidStatusTransitionException(
                    String.format("Admin user cannot transition order to status %s", newStatus)
                );
            }
        } else {
            throw new UnauthorizedTransitionException("Unsupported role for status transition");
        }

        return orderRepository.save(order);
    }
}

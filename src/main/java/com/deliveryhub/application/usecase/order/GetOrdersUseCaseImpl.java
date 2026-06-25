package com.deliveryhub.application.usecase.order;

import com.deliveryhub.application.port.in.order.GetOrdersUseCase;
import com.deliveryhub.application.port.out.order.OrderRepositoryPort;
import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del caso de uso GetOrdersUseCase.
 */
@Service
@Transactional(readOnly = true)
public class GetOrdersUseCaseImpl implements GetOrdersUseCase {

    private final OrderRepositoryPort orderRepository;

    /**
     * Constructor del caso de uso.
     *
     * @param orderRepository puerto de persistencia de pedidos
     */
    public GetOrdersUseCaseImpl(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Page<Order> execute(UUID requesterId, String requesterRoleName, OrderStatus statusFilter, Pageable pageable) {
        UserRole requesterRole = UserRole.valueOf(requesterRoleName);

        // Si es ADMIN, ve todas las órdenes
        if (requesterRole == UserRole.ADMIN) {
            if (statusFilter != null) {
                return orderRepository.findAllByStatus(statusFilter, pageable);
            }
            return orderRepository.findAll(pageable);
        } 
        // Si es DELIVERY, ve únicamente las suyas asignadas
        else if (requesterRole == UserRole.DELIVERY) {
            if (statusFilter != null) {
                return orderRepository.findAllByDeliveryUserIdAndStatus(requesterId, statusFilter, pageable);
            }
            return orderRepository.findAllByDeliveryUserId(requesterId, pageable);
        }

        return Page.empty();
    }
}

package com.deliveryhub.application.usecase.order;

import com.deliveryhub.application.port.in.order.CreateOrderUseCase;
import com.deliveryhub.application.port.out.order.OrderRepositoryPort;
import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del caso de uso CreateOrderUseCase.
 */
@Service
@Transactional
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    /**
     * Constructor del caso de uso.
     *
     * @param orderRepository puerto de persistencia de pedidos
     */
    public CreateOrderUseCaseImpl(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order execute(UUID adminUserId, String customerName, String customerPhone, 
                          Address address, String notes) {
        Order order = Order.create(adminUserId, customerName, customerPhone, address, notes);
        return orderRepository.save(order);
    }
}

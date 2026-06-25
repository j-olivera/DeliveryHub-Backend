package com.deliveryhub.infrastructure.adapter;

import com.deliveryhub.application.port.out.order.OrderRepositoryPort;
import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.model.Order;
import com.deliveryhub.infrastructure.entity.order.OrderEntity;
import com.deliveryhub.infrastructure.mapper.OrderMapper;
import com.deliveryhub.infrastructure.persistence.OrderJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el puerto OrderRepositoryPort.
 * Conecta el caso de uso con la persistencia real en la base de datos PostgreSQL.
 */
@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    /**
     * Constructor del adaptador.
     *
     * @param jpaRepository el repositorio JPA real
     * @param mapper        el mapeador de entidades JPA a dominio
     */
    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository, OrderMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Order> findAllByStatus(OrderStatus status, Pageable pageable) {
        return jpaRepository.findAllByStatus(status, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Order> findAllByDeliveryUserId(UUID deliveryUserId, Pageable pageable) {
        return jpaRepository.findAllByDeliveryUserId(deliveryUserId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Order> findAllByDeliveryUserIdAndStatus(UUID deliveryUserId, OrderStatus status, Pageable pageable) {
        return jpaRepository.findAllByDeliveryUserIdAndStatus(deliveryUserId, status, pageable).map(mapper::toDomain);
    }

    @Override
    public boolean hasActiveOrders(UUID deliveryUserId) {
        return jpaRepository.existsByDeliveryUserIdAndStatusIn(
            deliveryUserId,
            java.util.List.of(OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS)
        );
    }
}

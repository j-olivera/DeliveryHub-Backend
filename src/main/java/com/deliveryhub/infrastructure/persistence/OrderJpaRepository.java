package com.deliveryhub.infrastructure.persistence;

import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.infrastructure.entity.order.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio JPA para la entidad OrderEntity.
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    /**
     * Busca todos los pedidos asignados a un repartidor con paginación.
     *
     * @param deliveryUserId ID del repartidor
     * @param pageable       parámetros de paginación
     * @return página de pedidos
     */
    Page<OrderEntity> findAllByDeliveryUserId(UUID deliveryUserId, Pageable pageable);

    /**
     * Busca todos los pedidos con un estado específico con paginación.
     *
     * @param status   estado del pedido
     * @param pageable parámetros de paginación
     * @return página de pedidos
     */
    Page<OrderEntity> findAllByStatus(OrderStatus status, Pageable pageable);

    /**
     * Busca todos los pedidos de un repartidor con un estado específico con paginación.
     *
     * @param deliveryUserId ID del repartidor
     * @param status         estado del pedido
     * @param pageable       parámetros de paginación
     * @return página de pedidos
     */
    Page<OrderEntity> findAllByDeliveryUserIdAndStatus(UUID deliveryUserId, OrderStatus status, Pageable pageable);

    /**
     * Verifica si existe alguna orden asignada al repartidor con alguno de los estados provistos.
     *
     * @param deliveryUserId ID del repartidor
     * @param statuses       lista de estados a chequear
     * @return true si existe alguna orden que cumpla las condiciones, false en caso contrario
     */
    boolean existsByDeliveryUserIdAndStatusIn(UUID deliveryUserId, java.util.Collection<OrderStatus> statuses);
}

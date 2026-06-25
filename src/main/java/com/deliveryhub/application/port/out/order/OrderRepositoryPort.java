package com.deliveryhub.application.port.out.order;

import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de pedidos (Order).
 * Define las operaciones que la capa de aplicación requiere de la infraestructura.
 */
public interface OrderRepositoryPort {

    /**
     * Guarda un pedido en el sistema.
     *
     * @param order el pedido de dominio a guardar
     * @return el pedido guardado
     */
    Order save(Order order);

    /**
     * Busca un pedido por su ID.
     *
     * @param id ID del pedido
     * @return un Optional con el pedido si existe
     */
    Optional<Order> findById(UUID id);

    /**
     * Obtiene una página con todos los pedidos en el sistema.
     *
     * @param pageable parámetros de paginación
     * @return página de pedidos
     */
    Page<Order> findAll(Pageable pageable);

    /**
     * Obtiene una página con los pedidos filtrados por estado.
     *
     * @param status   estado del pedido
     * @param pageable parámetros de paginación
     * @return página de pedidos
     */
    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    /**
     * Obtiene una página con los pedidos asignados a un repartidor específico.
     *
     * @param deliveryUserId ID del repartidor
     * @param pageable       parámetros de paginación
     * @return página de pedidos
     */
    Page<Order> findAllByDeliveryUserId(UUID deliveryUserId, Pageable pageable);

    /**
     * Obtiene una página con los pedidos asignados a un repartidor filtrados por estado.
     *
     * @param deliveryUserId ID del repartidor
     * @param status         estado del pedido
     * @param pageable       parámetros de paginación
     * @return página de pedidos
     */
    Page<Order> findAllByDeliveryUserIdAndStatus(UUID deliveryUserId, OrderStatus status, Pageable pageable);

    /**
     * Verifica si un repartidor tiene pedidos activos (ASSIGNED o IN_PROGRESS).
     *
     * @param deliveryUserId ID del repartidor
     * @return true si tiene pedidos activos, false en caso contrario
     */
    boolean hasActiveOrders(UUID deliveryUserId);
}

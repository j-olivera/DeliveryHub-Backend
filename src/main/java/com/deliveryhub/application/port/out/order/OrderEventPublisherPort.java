package com.deliveryhub.application.port.out.order;

import com.deliveryhub.domain.model.Order;

/**
 * Puerto de salida para publicar eventos de pedidos en tiempo real a través de WebSockets.
 */
public interface OrderEventPublisherPort {

    /**
     * Publica un evento de pedido creado (notifica a ADMIN).
     *
     * @param order el pedido creado
     */
    void publishOrderCreated(Order order);

    /**
     * Publica un evento de pedido asignado (notifica a ADMIN + al DELIVERY asignado).
     *
     * @param order el pedido asignado
     */
    void publishOrderAssigned(Order order);

    /**
     * Publica un evento de estado de pedido actualizado (notifica a ADMIN).
     *
     * @param order el pedido actualizado
     */
    void publishOrderUpdated(Order order);

    /**
     * Publica un evento de pedido cancelado (notifica a ADMIN + al DELIVERY asignado si aplica).
     *
     * @param order el pedido cancelado
     */
    void publishOrderCancelled(Order order);
}

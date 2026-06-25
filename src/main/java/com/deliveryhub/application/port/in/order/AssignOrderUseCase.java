package com.deliveryhub.application.port.in.order;

import com.deliveryhub.domain.model.Order;
import java.util.UUID;

/**
 * Caso de uso para asignar un repartidor a un pedido pendiente.
 */
public interface AssignOrderUseCase {

    /**
     * Asigna un repartidor a un pedido en estado PENDING.
     *
     * @param orderId        ID del pedido
     * @param deliveryUserId ID del repartidor
     * @param adminUserId    ID del administrador que realiza la asignación
     * @return el pedido actualizado
     */
    Order execute(UUID orderId, UUID deliveryUserId, UUID adminUserId);
}

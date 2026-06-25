package com.deliveryhub.application.port.in.order;

import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.model.Order;
import java.util.UUID;

/**
 * Caso de uso para actualizar el estado del pedido, validando roles y pertenencia.
 */
public interface UpdateOrderStatusUseCase {

    /**
     * Actualiza el estado del pedido.
     *
     * @param orderId           ID del pedido
     * @param newStatus         nuevo estado deseado
     * @param requesterId       ID del usuario que realiza la solicitud
     * @param requesterRoleName rol del usuario que realiza la solicitud
     * @return el pedido actualizado
     */
    Order execute(UUID orderId, OrderStatus newStatus, UUID requesterId, String requesterRoleName);
}

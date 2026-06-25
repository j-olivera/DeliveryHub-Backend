package com.deliveryhub.application.port.in.order;

import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Caso de uso para obtener pedidos paginados y filtrados según el rol del usuario solicitante.
 */
public interface GetOrdersUseCase {

    /**
     * Obtiene una página de pedidos respetando las reglas de visibilidad de roles.
     *
     * @param requesterId       ID del usuario que solicita
     * @param requesterRoleName rol del usuario que solicita
     * @param statusFilter      filtro opcional por estado
     * @param pageable          parámetros de paginación
     * @return página de pedidos correspondiente
     */
    Page<Order> execute(UUID requesterId, String requesterRoleName, OrderStatus statusFilter, Pageable pageable);
}

package com.deliveryhub.application.port.in.order;

import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import java.util.UUID;

/**
 * Caso de uso para la creación de un nuevo pedido en estado PENDING.
 */
public interface CreateOrderUseCase {

    /**
     * Crea y persiste un nuevo pedido.
     *
     * @param adminUserId     ID del administrador que crea el pedido
     * @param customerName    nombre del cliente
     * @param customerPhone   teléfono del cliente
     * @param address         dirección del cliente
     * @param notes           notas aclaratorias
     * @return el pedido creado
     */
    Order execute(UUID adminUserId, String customerName, String customerPhone, 
                  Address address, String notes);
}

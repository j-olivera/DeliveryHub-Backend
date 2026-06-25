package com.deliveryhub.domain.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra un pedido en el sistema.
 */
public class OrderNotFoundException extends RuntimeException {

    /**
     * Constructor de la excepción.
     *
     * @param orderId ID del pedido no encontrado
     */
    public OrderNotFoundException(UUID orderId) {
        super(String.format("Order with ID %s not found", orderId));
    }
}

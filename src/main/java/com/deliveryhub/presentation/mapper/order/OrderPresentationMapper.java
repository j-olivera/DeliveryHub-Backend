package com.deliveryhub.presentation.mapper.order;

import com.deliveryhub.domain.model.Order;
import com.deliveryhub.presentation.dto.order.*;
import com.deliveryhub.presentation.dto.user.*;
import org.springframework.stereotype.Component;

/**
 * Mapeador manual para la capa de presentación de pedidos.
 * Convierte entre objetos de dominio Order y DTOs de respuesta de la API.
 */
@Component
public class OrderPresentationMapper {

    /**
     * Mapea un pedido de dominio al DTO OrderCreatedResponse.
     *
     * @param domain el pedido de dominio
     * @return DTO de respuesta
     */
    public OrderCreatedResponse toCreatedResponse(Order domain) {
        if (domain == null) {
            return null;
        }
        return new OrderCreatedResponse(
            domain.getId(),
            domain.getStatus().name(),
            domain.getCreatedAt()
        );
    }

    /**
     * Mapea un pedido de dominio al DTO OrderAssignedResponse.
     *
     * @param domain el pedido de dominio
     * @return DTO de respuesta
     */
    public OrderAssignedResponse toAssignedResponse(Order domain) {
        if (domain == null) {
            return null;
        }
        return new OrderAssignedResponse(
            domain.getId(),
            domain.getStatus().name(),
            domain.getDeliveryUserId()
        );
    }

    /**
     * Mapea un pedido de dominio al DTO OrderStatusUpdatedResponse.
     *
     * @param domain el pedido de dominio
     * @return DTO de respuesta
     */
    public OrderStatusUpdatedResponse toStatusUpdatedResponse(Order domain) {
        if (domain == null) {
            return null;
        }
        return new OrderStatusUpdatedResponse(
            domain.getId(),
            domain.getStatus().name(),
            domain.getUpdatedAt()
        );
    }

    /**
     * Mapea un pedido de dominio al DTO OrderResponse genérico para listados y detalles.
     *
     * @param domain el pedido de dominio
     * @return DTO de respuesta
     */
    public OrderResponse toResponse(Order domain) {
        if (domain == null) {
            return null;
        }

        CoordinatesResponse coords = null;
        AddressResponse addressResp = null;

        if (domain.getCustomerAddress() != null) {
            coords = new CoordinatesResponse(
                domain.getCustomerAddress().latitude(),
                domain.getCustomerAddress().longitude()
            );
            addressResp = new AddressResponse(
                domain.getCustomerAddress().street(),
                domain.getCustomerAddress().city(),
                coords
            );
        }

        return new OrderResponse(
            domain.getId(),
            domain.getCustomerName(),
            domain.getStatus().name(),
            addressResp,
            domain.getDeliveryUserId(),
            domain.getCreatedAt()
        );
    }
}

package com.deliveryhub.infrastructure.mapper;

import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import com.deliveryhub.infrastructure.entity.order.AddressEntity;
import com.deliveryhub.infrastructure.entity.order.OrderEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper manual para mapear entre la entidad de dominio Order y la entidad JPA OrderEntity,
 * incluyendo el mapeo de direcciones.
 */
@Component
public class OrderMapper {

    /**
     * Mapea un OrderEntity a la entidad de dominio Order.
     *
     * @param entity la entidad JPA a mapear
     * @return la entidad de dominio Order
     */
    public Order toDomain(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        Address address = toDomain(entity.getAddress());

        return Order.reconstruct(
            entity.getId(),
            entity.getAdminUserId(),
            entity.getCustomerName(),
            entity.getCustomerPhone(),
            address,
            entity.getNotes(),
            entity.getStatus(),
            entity.getDeliveryUserId(),
            entity.getCreatedAt(),
            entity.getAssignedAt(),
            entity.getDeliveredAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * Mapea la entidad de dominio Order a un OrderEntity.
     *
     * @param domain el pedido de dominio a mapear
     * @return la entidad JPA OrderEntity
     */
    public OrderEntity toEntity(Order domain) {
        if (domain == null) {
            return null;
        }

        OrderEntity entity = new OrderEntity();
        entity.setId(domain.getId());
        entity.setAdminUserId(domain.getAdminUserId());
        entity.setCustomerName(domain.getCustomerName());
        entity.setCustomerPhone(domain.getCustomerPhone());
        entity.setStatus(domain.getStatus());
        entity.setDeliveryUserId(domain.getDeliveryUserId());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setAssignedAt(domain.getAssignedAt());
        entity.setDeliveredAt(domain.getDeliveredAt());

        if (domain.getCustomerAddress() != null) {
            AddressEntity addressEntity = toEntity(domain.getCustomerAddress());
            addressEntity.setOrder(entity);
            entity.setAddress(addressEntity);
        }

        return entity;
    }

    /**
     * Mapea un AddressEntity a la entidad de dominio Address.
     *
     * @param entity la entidad JPA AddressEntity a mapear
     * @return el objeto de dominio Address
     */
    public Address toDomain(AddressEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Address(
            entity.getStreet(),
            entity.getCity(),
            entity.getLatitude(),
            entity.getLongitude()
        );
    }

    /**
     * Mapea la entidad de dominio Address a un AddressEntity.
     *
     * @param domain el objeto de dominio Address a mapear
     * @return la entidad JPA AddressEntity
     */
    public AddressEntity toEntity(Address domain) {
        if (domain == null) {
            return null;
        }
        AddressEntity entity = new AddressEntity();
        entity.setId(UUID.randomUUID());
        entity.setStreet(domain.street());
        entity.setCity(domain.city());
        entity.setLatitude(domain.latitude());
        entity.setLongitude(domain.longitude());
        return entity;
    }
}

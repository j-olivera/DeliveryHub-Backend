package com.deliveryhub.domain.model;

import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.exception.InvalidStatusTransitionException;
import com.deliveryhub.domain.exception.UnauthorizedTransitionException;
import com.deliveryhub.domain.exception.DomainValidationException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio que representa un Pedido (Order) en el sistema.
 * Es el agregado raíz del dominio de pedidos.
 */
public class Order {
    private final UUID id;
    private final UUID adminUserId;
    private final String customerName;
    private final String customerPhone;
    private final Address customerAddress;
    private final String notes;
    private OrderStatus status;
    private UUID deliveryUserId;
    private final LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime updatedAt;

    private Order(UUID id, UUID adminUserId, String customerName, String customerPhone, Address customerAddress, 
                  String notes, OrderStatus status, UUID deliveryUserId, LocalDateTime createdAt, 
                  LocalDateTime assignedAt, LocalDateTime deliveredAt, LocalDateTime updatedAt) {
        this.id = id;
        this.adminUserId = adminUserId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.notes = notes;
        this.status = status;
        this.deliveryUserId = deliveryUserId;
        this.createdAt = createdAt;
        this.assignedAt = assignedAt;
        this.deliveredAt = deliveredAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Crea una nueva instancia de un Pedido en estado PENDING.
     *
     * @param adminUserId     ID del administrador que crea el pedido
     * @param customerName    nombre del cliente
     * @param customerPhone   teléfono del cliente
     * @param customerAddress dirección de entrega del cliente
     * @param notes           notas adicionales para la entrega
     * @return un nuevo Pedido
     * @throws DomainValidationException si alguna regla de negocio es violada
     */
    public static Order create(UUID adminUserId, String customerName, String customerPhone, Address customerAddress, String notes) {
        if (adminUserId == null) {
            throw new DomainValidationException("Admin user ID cannot be null");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new DomainValidationException("Customer name cannot be blank");
        }
        if (customerAddress == null) {
            throw new DomainValidationException("Customer address cannot be null");
        }
        LocalDateTime now = LocalDateTime.now();
        return new Order(
            UUID.randomUUID(),
            adminUserId,
            customerName,
            customerPhone,
            customerAddress,
            notes,
            OrderStatus.PENDING,
            null,
            now,
            null,
            null,
            now
        );
    }

    /**
     * Reconstruye un pedido existente desde la persistencia.
     */
    public static Order reconstruct(UUID id, UUID adminUserId, String customerName, String customerPhone, Address customerAddress, 
                                     String notes, OrderStatus status, UUID deliveryUserId, LocalDateTime createdAt, 
                                     LocalDateTime assignedAt, LocalDateTime deliveredAt, LocalDateTime updatedAt) {
        return new Order(id, adminUserId, customerName, customerPhone, customerAddress, notes, status, deliveryUserId, 
                         createdAt, assignedAt, deliveredAt, updatedAt);
    }

    /**
     * Asigna un repartidor al pedido. Solo puede ser realizado por un ADMIN y si el estado es PENDING.
     */
    public void assign(UUID deliveryUserId, UserRole requesterRole) {
        if (requesterRole != UserRole.ADMIN) {
            throw new UnauthorizedTransitionException("Only ADMIN can assign orders");
        }
        if (deliveryUserId == null) {
            throw new DomainValidationException("Delivery user ID cannot be null");
        }
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidStatusTransitionException("Order must be PENDING to be assigned");
        }
        this.status = OrderStatus.ASSIGNED;
        this.deliveryUserId = deliveryUserId;
        this.assignedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Inicia el proceso de entrega de un pedido. Solo puede ser realizado por el repartidor asignado.
     */
    public void startDelivery(UUID requesterId, UserRole requesterRole) {
        if (requesterRole != UserRole.DELIVERY) {
            throw new UnauthorizedTransitionException("Only DELIVERY role can start delivery");
        }
        if (this.status != OrderStatus.ASSIGNED) {
            throw new InvalidStatusTransitionException("Order must be ASSIGNED to start delivery");
        }
        if (this.deliveryUserId == null || !this.deliveryUserId.equals(requesterId)) {
            throw new UnauthorizedTransitionException("Only the assigned delivery user can start the delivery");
        }
        this.status = OrderStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Completa la entrega de un pedido. Solo puede ser realizado por el repartidor asignado.
     */
    public void completeDelivery(UUID requesterId, UserRole requesterRole) {
        if (requesterRole != UserRole.DELIVERY) {
            throw new UnauthorizedTransitionException("Only DELIVERY role can complete delivery");
        }
        if (this.status != OrderStatus.IN_PROGRESS) {
            throw new InvalidStatusTransitionException("Order must be IN_PROGRESS to complete delivery");
        }
        if (this.deliveryUserId == null || !this.deliveryUserId.equals(requesterId)) {
            throw new UnauthorizedTransitionException("Only the assigned delivery user can complete the delivery");
        }
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancela el pedido. Solo puede ser realizado por un ADMIN y desde cualquier estado no terminal.
     */
    public void cancel(UserRole requesterRole) {
        if (requesterRole != UserRole.ADMIN) {
            throw new UnauthorizedTransitionException("Only ADMIN can cancel orders");
        }
        if (this.status == OrderStatus.DELIVERED || this.status == OrderStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Cannot cancel an order in a terminal state");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UUID getAdminUserId() {
        return adminUserId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public Address getCustomerAddress() {
        return customerAddress;
    }

    public String getNotes() {
        return notes;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public UUID getDeliveryUserId() {
        return deliveryUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

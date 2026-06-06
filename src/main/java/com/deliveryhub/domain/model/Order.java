package com.deliveryhub.domain.model;

import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.exception.InvalidStatusTransitionException;
import com.deliveryhub.domain.exception.UnauthorizedTransitionException;

import java.time.LocalDateTime;
import java.util.UUID;

public class Order {
    private final UUID id;
    private final UUID customerId;
    private final Address customerAddress;
    private OrderStatus status;
    private UUID deliveryUserId;
    private final LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime updatedAt;

    private Order(UUID id, UUID customerId, Address customerAddress, OrderStatus status, UUID deliveryUserId, LocalDateTime createdAt, LocalDateTime assignedAt, LocalDateTime deliveredAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.customerAddress = customerAddress;
        this.status = status;
        this.deliveryUserId = deliveryUserId;
        this.createdAt = createdAt;
        this.assignedAt = assignedAt;
        this.deliveredAt = deliveredAt;
        this.updatedAt = updatedAt;
    }

    public static Order create(UUID customerId, Address customerAddress) {
        LocalDateTime now = LocalDateTime.now();
        return new Order(
            UUID.randomUUID(),
            customerId,
            customerAddress,
            OrderStatus.PENDING,
            null,
            now,
            null,
            null,
            now
        );
    }

    public static Order reconstruct(UUID id, UUID customerId, Address customerAddress, OrderStatus status, UUID deliveryUserId, LocalDateTime createdAt, LocalDateTime assignedAt, LocalDateTime deliveredAt, LocalDateTime updatedAt) {
        return new Order(id, customerId, customerAddress, status, deliveryUserId, createdAt, assignedAt, deliveredAt, updatedAt);
    }

    public void assign(UUID deliveryUserId, UserRole requesterRole) {
        if (requesterRole != UserRole.ADMIN) {
            throw new UnauthorizedTransitionException("Only ADMIN can assign orders");
        }
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidStatusTransitionException("Order must be PENDING to be assigned");
        }
        this.status = OrderStatus.ASSIGNED;
        this.deliveryUserId = deliveryUserId;
        this.assignedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void startDelivery(UUID requesterId, UserRole requesterRole) {
        if (requesterRole != UserRole.DELIVERY) {
            throw new UnauthorizedTransitionException("Only DELIVERY role can start delivery");
        }
        if (this.status != OrderStatus.ASSIGNED) {
            throw new InvalidStatusTransitionException("Order must be ASSIGNED to start delivery");
        }
        if (!this.deliveryUserId.equals(requesterId)) {
            throw new UnauthorizedTransitionException("Only the assigned delivery user can start the delivery");
        }
        this.status = OrderStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void completeDelivery(UUID requesterId, UserRole requesterRole) {
        if (requesterRole != UserRole.DELIVERY) {
            throw new UnauthorizedTransitionException("Only DELIVERY role can complete delivery");
        }
        if (this.status != OrderStatus.IN_PROGRESS) {
            throw new InvalidStatusTransitionException("Order must be IN_PROGRESS to complete delivery");
        }
        if (!this.deliveryUserId.equals(requesterId)) {
            throw new UnauthorizedTransitionException("Only the assigned delivery user can complete the delivery");
        }
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

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

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public Address getCustomerAddress() {
        return customerAddress;
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

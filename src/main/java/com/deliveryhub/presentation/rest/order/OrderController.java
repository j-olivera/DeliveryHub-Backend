package com.deliveryhub.presentation.rest.order;

import com.deliveryhub.application.port.in.order.AssignOrderUseCase;
import com.deliveryhub.application.port.in.order.CreateOrderUseCase;
import com.deliveryhub.application.port.in.order.GetOrdersUseCase;
import com.deliveryhub.application.port.in.order.UpdateOrderStatusUseCase;
import com.deliveryhub.application.port.out.user.UserRepositoryPort;
import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.exception.DomainValidationException;
import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.presentation.dto.order.*;
import com.deliveryhub.presentation.dto.user.*;
import com.deliveryhub.presentation.mapper.order.OrderPresentationMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Controlador REST para la gestión de pedidos (órdenes).
 * Expone endpoints para crear, asignar, actualizar estado, listar y cancelar pedidos.
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final AssignOrderUseCase assignOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final GetOrdersUseCase getOrdersUseCase;
    private final UserRepositoryPort userRepositoryPort;
    private final OrderPresentationMapper orderPresentationMapper;

    /**
     * Constructor del controlador.
     *
     * @param createOrderUseCase caso de uso de creación de pedido
     * @param assignOrderUseCase caso de uso de asignación de pedido
     * @param updateOrderStatusUseCase caso de uso de actualización de estado
     * @param getOrdersUseCase caso de uso para listar pedidos
     * @param userRepositoryPort puerto para buscar información del usuario autenticado
     * @param orderPresentationMapper mapeador de presentación
     */
    public OrderController(CreateOrderUseCase createOrderUseCase,
                           AssignOrderUseCase assignOrderUseCase,
                           UpdateOrderStatusUseCase updateOrderStatusUseCase,
                           GetOrdersUseCase getOrdersUseCase,
                           UserRepositoryPort userRepositoryPort,
                           OrderPresentationMapper orderPresentationMapper) {
        this.createOrderUseCase = createOrderUseCase;
        this.assignOrderUseCase = assignOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.getOrdersUseCase = getOrdersUseCase;
        this.userRepositoryPort = userRepositoryPort;
        this.orderPresentationMapper = orderPresentationMapper;
    }

    /**
     * Crea un nuevo pedido.
     *
     * @param request DTO con los datos del pedido a crear
     * @return DTO de respuesta con los datos de creación y estado 201 Created
     */
    @PostMapping
    public ResponseEntity<OrderCreatedResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        User currentUser = getCurrentUser();
        Address address = new Address(
            request.address().street(),
            request.address().city(),
            request.address().coordinates().lat(),
            request.address().coordinates().lng()
        );
        Order order = createOrderUseCase.execute(
            currentUser.getId(),
            request.customerName(),
            request.customerPhone(),
            address,
            request.notes()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(orderPresentationMapper.toCreatedResponse(order));
    }

    /**
     * Asigna un repartidor a un pedido.
     *
     * @param id ID del pedido
     * @param request DTO con el ID del repartidor
     * @return DTO de respuesta con los datos de la asignación
     */
    @PatchMapping("/{id}/assign")
    public ResponseEntity<OrderAssignedResponse> assignOrder(@PathVariable UUID id, @Valid @RequestBody AssignOrderRequest request) {
        User currentUser = getCurrentUser();
        Order order = assignOrderUseCase.execute(id, request.deliveryUserId(), currentUser.getId());
        return ResponseEntity.ok(orderPresentationMapper.toAssignedResponse(order));
    }

    /**
     * Actualiza el estado de un pedido.
     *
     * @param id ID del pedido
     * @param request DTO con el nuevo estado
     * @return DTO de respuesta con los datos del estado actualizado
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderStatusUpdatedResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        User currentUser = getCurrentUser();
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.status());
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException("Invalid status value: " + request.status());
        }
        Order order = updateOrderStatusUseCase.execute(id, newStatus, currentUser.getId(), currentUser.getRole().name());
        return ResponseEntity.ok(orderPresentationMapper.toStatusUpdatedResponse(order));
    }

    /**
     * Cancela un pedido (cambia su estado a CANCELLED).
     *
     * @param id ID del pedido
     * @return respuesta vacía con código HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        updateOrderStatusUseCase.execute(id, OrderStatus.CANCELLED, currentUser.getId(), currentUser.getRole().name());
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene una lista paginada de pedidos según los permisos del usuario actual.
     *
     * @param status filtro opcional por estado del pedido
     * @param pageable parámetros de paginación
     * @return página con pedidos en formato DTO de respuesta
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(@RequestParam(required = false) String status, Pageable pageable) {
        User currentUser = getCurrentUser();
        OrderStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = OrderStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new DomainValidationException("Invalid status filter: " + status);
            }
        }
        Page<Order> ordersPage = getOrdersUseCase.execute(currentUser.getId(), currentUser.getRole().name(), statusFilter, pageable);
        Page<OrderResponse> responsePage = ordersPage.map(orderPresentationMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepositoryPort.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}

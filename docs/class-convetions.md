# Convenciones de clases — DeliveryHub

## Domain

### Entidad de dominio
```java
// Nombre: PascalCase, sustantivo en singular
// Ubicación: domain/model/
public final class Order {

    private final UUID id;
    private OrderStatus status;
    private final UUID adminUserId;
    private UUID deliveryUserId; // nullable hasta que se asigne

    // Constructor privado y completo
    private Order(UUID id, OrderStatus status, UUID adminUserId) {
        this.id = id;
        this.status = status;
        this.adminUserId = adminUserId;
    }

    // Método fábrica para crear nuevas instancias
    public static Order create(UUID id, OrderStatus status, UUID adminUserId) {
        return new Order(null, status, adminUserId);
    }

    public static Order reconstruct(UUID id, OrderStatus status, UUID adminUserId) {
        return new Order(id, status, adminUserId);
    }

    // La lógica de transición vive en la entidad, no en el use case
    public void assignTo(UUID deliveryUserId) {
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidStatusTransitionException(this.id, this.status, OrderStatus.ASSIGNED);
        }
        this.deliveryUserId = deliveryUserId;
        this.status = OrderStatus.ASSIGNED;
    }
}
```

### Enum de dominio
```java
// Ubicación: domain/enums/
public enum OrderStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    DELIVERED,
    CANCELLED
}
```

### Excepción de dominio
```java
// Nombre: [Concepto][Motivo]Exception
// Ubicación: domain/exception/
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(UUID orderId, OrderStatus from, OrderStatus to) {
        super(String.format(
            "Transición inválida para pedido %s: %s → %s", orderId, from, to
        ));
    }
}
```

---

## Application

### Port de entrada (interface)
```java
// Nombre: [Verbo][Entidad]UseCase
// Ubicación: application/port/in/
// En caso de tener tipo -> Response, si no tiene -> void
public interface AssignOrderUseCase {
    void execute(UUID orderId, UUID deliveryUserId, UUID requestingUserId);
}
```

### Port de salida (interface)
```java
// Nombre: [Entidad]RepositoryPort
// Ubicación: application/port/out/
public interface OrderRepositoryPort {
    Order findById(UUID id);
    Order save(Order order);
    List<Order> findAllByDeliveryUserId(UUID deliveryUserId);
}
```

### Caso de uso (implementación)
```java
// Nombre: [Verbo][Entidad]UseCaseImpl
// Ubicación: application/usecase/
// Siempre @Service y @Transactional si modifica datos
@Service
@Transactional
public class AssignOrderUseCaseImpl implements AssignOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final UserRepositoryPort userRepository;

    // Inyección por constructor, siempre
    public AssignOrderUseCaseImpl(
        OrderRepositoryPort orderRepository,
        UserRepositoryPort userRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void execute(UUID orderId, UUID deliveryUserId, UUID requestingUserId) {
        // La lógica de negocio delega al dominio
        Order order = orderRepository.findById(orderId);
        order.assignTo(deliveryUserId);
        orderRepository.save(order);
    }
}
```

---

## Infrastructure

### Entidad JPA
```java
// Nombre: [Entidad]Entity
// Ubicación: infrastructure/persistence/entity/
// Las entidades JPA son distintas a las de dominio
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "delivery_user_id")
    private UUID deliveryUserId;
}
```

### Repositorio JPA
```java
// Nombre: [Entidad]JpaRepository
// Ubicación: infrastructure/persistence/
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByDeliveryUserId(UUID deliveryUserId);
}
```

### Adaptador de repositorio (implementa el port)
```java
// Nombre: [Entidad]RepositoryAdapter
// Ubicación: infrastructure/persistence/
@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository, OrderMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
```

### Mapper (MapStruct)
```java
// Nombre: [Entidad]Mapper
// Ubicación: infrastructure/mapper/
@Component
public interface OrderMapper {
    Order toDomain(OrderEntity entity){
        return Order.reconstruct(entity.getId(), entity.getStatus(), entity.getDeliveryUserId());
    }
    OrderEntity toEntity(Order domain){
        OrderEntity entity = new OrderEntity();
        entity.setId(domain.getId());
        entity.setStatus(domain.getStatus());
        entity.setDeliveryUserId(domain.getDeliveryUserId());
        return entity;
    }
    OrderResponse toResponse(Order domain){
        OrderResponse response = new OrderResponse();
        response.setId(domain.getId());
        response.setStatus(domain.getStatus());
        response.setDeliveryUserId(domain.getDeliveryUserId());
        return response;
    }
}
```

---

## Presentation

### Controller REST
```java
// Nombre: [Entidad]Controller
// Ubicación: presentation/rest/
// Un controller por entidad, sin lógica de negocio
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final AssignOrderUseCase assignOrderUseCase;
    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(
        AssignOrderUseCase assignOrderUseCase,
        CreateOrderUseCase createOrderUseCase
    ) {
        this.assignOrderUseCase = assignOrderUseCase;
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        // Solo deserializar y delegar. Nada más.
        OrderResponse response = createOrderUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### DTO de request
```java
// Nombre: [Acción][Entidad]Request
// Ubicación: presentation/dto/
public record CreateOrderRequest(
    @NotBlank String customerName,
    @NotNull @Valid AddressRequest address,
    @NotNull UUID deliveryUserId
) {}
```

### DTO de response
```java
// Nombre: [Entidad]Response
// Ubicación: presentation/dto/
public record OrderResponse(
    UUID id,
    String customerName,
    OrderStatus status,
    UUID deliveryUserId,
    LocalDateTime createdAt
) {}
```

### Exception handler global
```java
// Un único @RestControllerAdvice en presentation/
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

## Nota
- Los codigos proporcionados son de ejemplo, se deben adaptar a la clase correspondiente.

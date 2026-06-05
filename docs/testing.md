# Testing — DeliveryHub

## Filosofía TDD

El ciclo es siempre: **Red → Green → Refactor**. No se escribe código de producción sin un test en rojo que lo justifique.

```
1. Escribir el test que describe el comportamiento deseado (falla)
2. Escribir el mínimo código para que el test pase
3. Refactorizar sin romper el test
4. Repetir
```

## Estrategia por capa

### Domain — JUnit 5 puro

Sin Spring, sin mocks. Las entidades deben ser testeables de forma aislada.

```java
class OrderTest {

    @Test
    void assignTo_whenOrderIsPending_shouldTransitionToAssigned() {
        // Arrange
        Order order = new Order(UUID.randomUUID(), OrderStatus.PENDING, UUID.randomUUID());
        UUID deliveryUserId = UUID.randomUUID();

        // Act
        order.assignTo(deliveryUserId);

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(order.getDeliveryUserId()).isEqualTo(deliveryUserId);
    }

    @Test
    void assignTo_whenOrderIsNotPending_shouldThrowInvalidStatusTransitionException() {
        Order order = new Order(UUID.randomUUID(), OrderStatus.ASSIGNED, UUID.randomUUID());

        assertThatThrownBy(() -> order.assignTo(UUID.randomUUID()))
            .isInstanceOf(InvalidStatusTransitionException.class);
    }
}
```

### Application — JUnit 5 + Mockito

Testear el caso de uso en aislamiento. Mockear los ports de salida, nunca el dominio.

```java
@ExtendWith(MockitoExtension.class)
class AssignOrderUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private OrderEventPublisherPort eventPublisher;

    @InjectMocks
    private AssignOrderUseCaseImpl useCase;

    @Test
    void execute_whenOrderIsPending_shouldAssignAndPublishEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID deliveryUserId = UUID.randomUUID();
        Order pendingOrder = new Order(orderId, OrderStatus.PENDING, UUID.randomUUID());

        when(orderRepository.findById(orderId)).thenReturn(pendingOrder);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        useCase.execute(orderId, deliveryUserId, UUID.randomUUID());

        // Assert
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.ASSIGNED));
        verify(eventPublisher).publishOrderUpdated(any());
    }

    @Test
    void execute_whenOrderIsNotFound_shouldThrowOrderNotFoundException() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenThrow(new OrderNotFoundException(orderId));

        assertThatThrownBy(() -> useCase.execute(orderId, UUID.randomUUID(), UUID.randomUUID()))
            .isInstanceOf(OrderNotFoundException.class);
    }
}
```

### Infrastructure — @DataJpaTest + Testcontainers

Testear repositorios contra una base de datos PostgreSQL real (no H2).

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class OrderRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("deliveryhub_test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderJpaRepository jpaRepository;

    @Test
    void findByDeliveryUserId_shouldReturnOnlyAssignedOrders() {
        // Test contra PostgreSQL real
    }
}
```

### Presentation — @WebMvcTest + MockMvc

Testear el contrato HTTP: URL, método, body, código de respuesta.

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createOrder_withValidRequest_shouldReturn201() throws Exception {
        // Arrange
        OrderResponse response = new OrderResponse(UUID.randomUUID(), "Juan", OrderStatus.PENDING, null, now());
        when(createOrderUseCase.execute(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "customerName": "Juan García",
                      "address": { "street": "Av. Rivadavia 1234", "city": "Buenos Aires" }
                    }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "DELIVERY")
    void createOrder_withDeliveryRole_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }
}
```

## Nombrado de tests

Formato: `método_escenario_resultadoEsperado`

```
assignTo_whenOrderIsPending_shouldTransitionToAssigned      ✅
assignTo_whenOrderIsNotPending_shouldThrowException         ✅
testAssign()                                                ❌
shouldWork()                                                ❌
```

## Reglas

- Un test verifica una sola cosa. Si necesita más de dos asserts relacionados, está bien. Si son conceptos distintos, separar en dos tests.
- No mockear el dominio. Nunca.
- Los tests de Application testean todos los caminos: flujo feliz + cada excepción posible.
- Cobertura mínima esperada en Application y Domain: 90%.
- Nombrar los mocks con lo que representan, no con el tipo: `orderRepository`, no `mock1`.
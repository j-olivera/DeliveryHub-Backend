package com.deliveryhub.infrastructure.websocket;

import com.deliveryhub.application.dto.order.OrderEvent;
import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.infrastructure.security.JwtUtils;
import com.deliveryhub.application.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import com.deliveryhub.infrastructure.persistence.UserJpaRepository;
import com.deliveryhub.infrastructure.persistence.OrderJpaRepository;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private OrderEventPublisherAdapter orderEventPublisherAdapter;

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    private String wsUrl;
    private WebSocketStompClient stompClient;
    private BlockingQueue<OrderEvent> blockingQueue;

    @BeforeEach
    void setUp() {
        wsUrl = "ws://localhost:" + port + "/ws";
        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        stompClient = new WebSocketStompClient(new SockJsClient(transports));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        blockingQueue = new LinkedBlockingQueue<>();
    }

    @AfterEach
    void tearDown() {
        if (stompClient != null) {
            stompClient.stop();
        }
        orderJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    private boolean hasAnySubscription() {
        return simpUserRegistry.getUsers().stream()
                .flatMap(user -> user.getSessions().stream())
                .anyMatch(session -> !session.getSubscriptions().isEmpty());
    }

    @Test
    void connect_whenTokenIsValid_shouldEstablishConnectionAndReceiveEvent() throws Exception {
        // Arrange: Crear usuario Admin y token JWT válido
        String email = "admin@deliveryhub.com";
        User adminUser = User.reconstruct(UUID.randomUUID(), "Admin User", email, "password", UserRole.ADMIN);
        userRepositoryPort.save(adminUser);

        String jwt = jwtUtils.generateToken(email);

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + jwt);

        StompSession stompSession = null;
        try {
            // Act: Conectar al WebSocket
            stompSession = stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
                    .get(5, TimeUnit.SECONDS);

            assertNotNull(stompSession);
            assertTrue(stompSession.isConnected());

            // Suscribirse a /topic/admin/orders
            stompSession.subscribe("/topic/admin/orders", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return OrderEvent.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    blockingQueue.offer((OrderEvent) payload);
                }
            });

            org.awaitility.Awaitility.await().atMost(2, TimeUnit.SECONDS)
                    .until(this::hasAnySubscription);

            // Simular evento
            Order order = Order.create(
                    adminUser.getId(),
                    "Cliente Test",
                    "+5491112345678",
                    new Address("Calle Falsa 123", "CABA", -34.6, -58.4),
                    "Notas"
            );
            orderEventPublisherAdapter.publishOrderCreated(order);

            // Assert: Verificar recepción del evento
            OrderEvent event = blockingQueue.poll(5, TimeUnit.SECONDS);
            assertNotNull(event);
            assertEquals("ORDER_CREATED", event.eventType());
            assertEquals(order.getId(), event.orderId());
            assertEquals(OrderStatus.PENDING.name(), event.newStatus());
        } finally {
            if (stompSession != null && stompSession.isConnected()) {
                stompSession.disconnect();
            }
        }
    }

    @Test
    void connect_whenTokenIsMissingOrInvalid_shouldFailToConnect() {
        StompHeaders connectHeaders = new StompHeaders();
        // Sin token o token inválido
        connectHeaders.add("Authorization", "Bearer invalid-token");

        // Act & Assert: Intentar conectar, debería lanzar excepción o fallar
        assertThrows(Exception.class, () -> {
            stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
                    .get(5, TimeUnit.SECONDS);
        });
    }

    @Test
    void connect_whenDeliveryUser_shouldReceivePrivateQueueEvents() throws Exception {
        // Arrange: Crear usuario Delivery y token JWT válido
        String email = "delivery@deliveryhub.com";
        UUID deliveryId = UUID.randomUUID();
        User deliveryUser = User.reconstruct(deliveryId, "Delivery User", email, "password", UserRole.DELIVERY);
        userRepositoryPort.save(deliveryUser);

        String jwt = jwtUtils.generateToken(email);

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + jwt);

        StompSession stompSession = null;
        try {
            // Act: Conectar al WebSocket
            stompSession = stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {
                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                    System.out.println(">>> CLIENT STOMP EXCEPTION: " + exception.getMessage());
                    exception.printStackTrace();
                }
                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    System.out.println(">>> CLIENT TRANSPORT ERROR: " + exception.getMessage());
                    exception.printStackTrace();
                }
            })
                    .get(5, TimeUnit.SECONDS);

            assertNotNull(stompSession);
            assertTrue(stompSession.isConnected());

            // Suscribirse a su cola privada /user/queue/orders
            stompSession.subscribe("/user/queue/orders", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return OrderEvent.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    blockingQueue.offer((OrderEvent) payload);
                }
            });

            org.awaitility.Awaitility.await().atMost(2, TimeUnit.SECONDS)
                    .until(this::hasAnySubscription);

            // Simular evento para este delivery
            UUID adminId = UUID.randomUUID();
            Order order = Order.reconstruct(
                    UUID.randomUUID(),
                    adminId,
                    "Cliente Test",
                    "+5491112345678",
                    new Address("Calle Falsa 123", "CABA", -34.6, -58.4),
                    "Notas",
                    OrderStatus.ASSIGNED,
                    deliveryId,
                    java.time.LocalDateTime.now(),
                    java.time.LocalDateTime.now(),
                    null,
                    java.time.LocalDateTime.now()
            );
            orderEventPublisherAdapter.publishOrderAssigned(order);

            // Assert: Verificar recepción del evento
            OrderEvent event = blockingQueue.poll(5, TimeUnit.SECONDS);
            assertNotNull(event);
            assertEquals("ORDER_ASSIGNED", event.eventType());
            assertEquals(order.getId(), event.orderId());
            assertEquals(OrderStatus.ASSIGNED.name(), event.newStatus());
        } finally {
            if (stompSession != null && stompSession.isConnected()) {
                stompSession.disconnect();
            }
        }
    }

    @Test
    void subscribe_whenUserIsNotAdmin_shouldFailForAdminTopic() throws Exception {
        // Arrange: Crear usuario Delivery y token JWT válido (no tiene ROLE_ADMIN)
        String email = "delivery-test@deliveryhub.com";
        User deliveryUser = User.reconstruct(UUID.randomUUID(), "Delivery User", email, "password", UserRole.DELIVERY);
        userRepositoryPort.save(deliveryUser);

        String jwt = jwtUtils.generateToken(email);

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + jwt);

        StompSession stompSession = null;
        try {
            // Act: Conectar al WebSocket
            stompSession = stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
                    .get(5, TimeUnit.SECONDS);

            assertNotNull(stompSession);
            assertTrue(stompSession.isConnected());

            // Intentar suscribirse a /topic/admin/orders
            StompSession finalSession = stompSession;
            finalSession.subscribe("/topic/admin/orders", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return OrderEvent.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    // no-op
                }
            });

            org.awaitility.Awaitility.await().atMost(2, TimeUnit.SECONDS)
                    .until(() -> !finalSession.isConnected());
        } finally {
            if (stompSession != null && stompSession.isConnected()) {
                stompSession.disconnect();
            }
        }
    }

    @Test
    void connect_whenUserIsInactive_shouldFailToConnect() {
        // Arrange: Crear usuario inactivo (active = false)
        String email = "inactive@deliveryhub.com";
        User inactiveUser = User.reconstruct(UUID.randomUUID(), "Inactive User", email, "password", UserRole.DELIVERY, false);
        userRepositoryPort.save(inactiveUser);

        String jwt = jwtUtils.generateToken(email);

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + jwt);

        // Act & Assert: Intentar conectar, debería lanzar excepción
        assertThrows(Exception.class, () -> {
            stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
                    .get(5, TimeUnit.SECONDS);
        });
    }

    @Test
    void send_whenDirectPublishingToBrokerTopic_shouldDisconnect() throws Exception {
        // Arrange: Crear usuario Admin y token JWT válido
        String email = "admin-send-test@deliveryhub.com";
        User adminUser = User.reconstruct(UUID.randomUUID(), "Admin User", email, "password", UserRole.ADMIN);
        userRepositoryPort.save(adminUser);

        String jwt = jwtUtils.generateToken(email);

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + jwt);

        StompSession stompSession = null;
        try {
            stompSession = stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
                    .get(5, TimeUnit.SECONDS);

            assertNotNull(stompSession);
            assertTrue(stompSession.isConnected());

            // Act: Intentar enviar directamente a /topic/admin/orders
            StompSession finalSession = stompSession;
            finalSession.send("/topic/admin/orders", "dummy payload");

            // Assert: Debería desconectarse debido al lanzamiento de MessageDeliveryException
            org.awaitility.Awaitility.await().atMost(2, TimeUnit.SECONDS)
                    .until(() -> !finalSession.isConnected());
        } finally {
            if (stompSession != null && stompSession.isConnected()) {
                stompSession.disconnect();
            }
        }
    }

    @Test
    void connect_whenAuthorizationHeaderIsMissing_shouldFailToConnect() {
        StompHeaders connectHeaders = new StompHeaders();
        // Sin cabecera Authorization

        // Act & Assert: Intentar conectar, debería lanzar excepción
        assertThrows(Exception.class, () -> {
            stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
                    .get(5, TimeUnit.SECONDS);
        });
    }
}

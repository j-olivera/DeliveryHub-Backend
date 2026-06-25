package com.deliveryhub.infrastructure.websocket;

import com.deliveryhub.application.dto.order.OrderEvent;
import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.infrastructure.security.JwtUtils;
import com.deliveryhub.application.port.out.user.UserRepositoryPort;
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

    @Test
    void connect_whenTokenIsValid_shouldEstablishConnectionAndReceiveEvent() throws Exception {
        // Arrange: Crear usuario Admin y token JWT válido
        String email = "admin@deliveryhub.com";
        User adminUser = User.reconstruct(UUID.randomUUID(), "Admin User", email, "password", UserRole.ADMIN);
        userRepositoryPort.save(adminUser);

        String jwt = jwtUtils.generateToken(email);

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + jwt);

        // Act: Conectar al WebSocket
        StompSession stompSession = stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
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

        stompSession.disconnect();
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
}

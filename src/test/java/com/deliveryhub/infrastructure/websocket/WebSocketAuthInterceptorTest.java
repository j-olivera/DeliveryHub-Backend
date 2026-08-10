package com.deliveryhub.infrastructure.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.deliveryhub.infrastructure.security.JwtUtils;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private WebSocketAuthInterceptor interceptor;

    private MessageChannel channel;

    @BeforeEach
    void setUp() {
        channel = mock(MessageChannel.class);
    }

    @Test
    void preSend_whenSendCommandAndUserIsNull_shouldThrowMessageDeliveryException() {
        // Arrange
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/app/test");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // Act & Assert
        assertThrows(MessageDeliveryException.class, () -> {
            interceptor.preSend(message, channel);
        }, "User is not authenticated");
    }

    @Test
    void preSend_whenSendCommandAndUserIsNotNullButDestinationIsInvalid_shouldThrowMessageDeliveryException() {
        // Arrange
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Principal principal = mock(Principal.class);
        accessor.setUser(principal);
        accessor.setDestination("/topic/invalid");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // Act & Assert
        assertThrows(MessageDeliveryException.class, () -> {
            interceptor.preSend(message, channel);
        }, "Direct publishing to broker destinations is not allowed");
    }
}

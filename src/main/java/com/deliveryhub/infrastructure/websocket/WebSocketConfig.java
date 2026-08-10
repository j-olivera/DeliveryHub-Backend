package com.deliveryhub.infrastructure.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket para la aplicación mediante STOMP.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * Constructor de WebSocketConfig.
     *
     * @param webSocketAuthInterceptor interceptor de autenticación WebSocket
     */
    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Habilitar brokers simples para destinos públicos y específicos de usuario
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Prefijo para los mensajes dirigidos a métodos anotados con @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefijo para enviar mensajes específicos a un usuario
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint de conexión de WebSocket con SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Registrar el interceptor de autenticación
        registration.interceptors(webSocketAuthInterceptor);
    }
}

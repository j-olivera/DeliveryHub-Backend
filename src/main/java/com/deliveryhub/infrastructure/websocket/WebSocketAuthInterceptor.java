package com.deliveryhub.infrastructure.websocket;

import com.deliveryhub.infrastructure.security.CustomUserDetails;
import com.deliveryhub.infrastructure.security.JwtUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.security.Principal;

/**
 * Interceptor de canal para realizar la autenticación mediante token JWT en las conexiones WebSocket (STOMP).
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Constructor del interceptor.
     *
     * @param jwtUtils las utilidades de JWT
     * @param userDetailsService el servicio de detalles de usuario
     */
    public WebSocketAuthInterceptor(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");

                if (authHeader == null) {
                    throw new MessageDeliveryException("Missing Authorization header");
                }
                if (!authHeader.startsWith("Bearer ")) {
                    throw new MessageDeliveryException("Invalid Authorization header format");
                }

                String jwt = authHeader.substring(7);

                if (jwtUtils.validateToken(jwt)) {
                    String username = jwtUtils.getUsernameFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (!userDetails.isEnabled()) {
                        throw new MessageDeliveryException("User is deactivated");
                    }

                    if (!(userDetails instanceof CustomUserDetails)) {
                        throw new MessageDeliveryException("CustomUserDetails implementation is required for WebSocket connections");
                    }
                    String principalName = ((CustomUserDetails) userDetails).getId().toString();

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            new StompPrincipal(principalName), null, userDetails.getAuthorities());
                    
                    accessor.setUser(authentication);
                } else {
                    throw new MessageDeliveryException("Invalid JWT token");
                }
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                if (accessor.getUser() == null) {
                    throw new MessageDeliveryException("User is not authenticated");
                }
                String destination = accessor.getDestination();
                if (destination != null) {
                    if (destination.contains("..") || destination.contains("//")) {
                        throw new MessageDeliveryException("Invalid characters in destination path");
                    }
                    if (pathMatcher.match("/queue/**", destination)) {
                        throw new MessageDeliveryException("Direct subscription to queue destinations is not allowed");
                    }
                    if (destination.startsWith("/user/")) {
                        if (!pathMatcher.match("/user/queue/**", destination)) {
                            throw new MessageDeliveryException("Invalid user subscription destination");
                        }
                    }
                    if (pathMatcher.match("/topic/admin/**", destination)) {
                        Principal user = accessor.getUser();
                        if (user == null) {
                            throw new MessageDeliveryException("Unauthorized topic subscription");
                        }
                        if (user instanceof UsernamePasswordAuthenticationToken) {
                            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) user;
                            boolean hasAdminRole = auth.getAuthorities().stream()
                                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
                            if (!auth.isAuthenticated() || !hasAdminRole) {
                                throw new MessageDeliveryException("Unauthorized topic subscription");
                            }
                        } else {
                            throw new MessageDeliveryException("Unauthorized topic subscription");
                        }
                    }
                }
            } else if (StompCommand.SEND.equals(accessor.getCommand())) {
                if (accessor.getUser() == null) {
                    throw new MessageDeliveryException("User is not authenticated");
                }
                String destination = accessor.getDestination();
                if (destination == null || !destination.startsWith("/app/")) {
                    throw new MessageDeliveryException("Direct publishing to broker destinations is not allowed");
                }
            }
        }

        return message;
    }

    /**
     * Principal personalizado para WebSockets que encapsula el ID de usuario.
     */
    private static class StompPrincipal implements Principal {
        private final String name;

        public StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}

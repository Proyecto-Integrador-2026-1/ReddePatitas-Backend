package com.redpatitas.redPatitas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefijo para mensajes enviados del servidor al cliente
        config.enableSimpleBroker("/topic", "/queue");
        // Prefijo para mensajes enviados del cliente al servidor
        config.setApplicationDestinationPrefixes("/app");
        // Prefijo para mensajes específicos de usuario
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint de conexión WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173", "http://localhost:3000")
                .withSockJS(); // Fallback para navegadores sin WebSocket
    }
}
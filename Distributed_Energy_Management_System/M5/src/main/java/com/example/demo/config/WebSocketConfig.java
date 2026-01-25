package com.example.demo.config;

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
        // 1. Enable a simple memory-based message broker
        // The Backend pushes messages to destinations starting with "/topic"
        config.enableSimpleBroker("/topic");

        // 2. Prefix for messages sent FROM client TO server (for Chat)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 3. The Endpoint: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow React (port 3000) to connect
                .withSockJS(); // Enable fallback options
    }
}
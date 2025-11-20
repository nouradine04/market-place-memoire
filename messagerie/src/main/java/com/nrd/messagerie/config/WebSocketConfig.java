package com.nrd.messagerie.config;

import com.nrd.messagerie.exception.MessageWebSocketHandler;
import com.nrd.messagerie.security.JwtUtil;
import com.nrd.messagerie.service.MessageService;
import com.nrd.messagerie.service.WebSocketSessionManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSocket
@Slf4j
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;
    private final WebSocketSessionManager sessionManager;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MessageWebSocketHandler(messageService, jwtUtil, sessionManager), "/ws")
                .setAllowedOriginPatterns("*");
        
        log.info("WebSocket endpoint /ws registered successfully on port 8083");
        log.info("WebSocket URL: ws://localhost:8083/ws?token=YOUR_JWT_TOKEN");
    }
}
package com.nrd.messagerie.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nrd.messagerie.entity.Message;
import com.nrd.messagerie.service.MessageService;
import com.nrd.messagerie.service.WebSocketSessionManager;
import com.nrd.messagerie.security.JwtUtil;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageWebSocketHandler.class);
    private final MessageService messageService;
    private final JwtUtil jwtUtil;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("🔗 WebSocket connection established for session: {} with URI: {}", session.getId(), session.getUri());

        String token = extractTokenFromUri(session.getUri());
        logger.info("🔍 Extracted token: {}", token != null ? "[PRESENT - Length: " + token.length() + "]" : "[MISSING]");

        if (token != null && !token.isEmpty()) {
            try {
                logger.info("🔐 Validating token...");
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    session.getAttributes().put("userId", userId);
                    session.getAttributes().put("token", token);
                    sessionManager.addSession(userId, session);
                    logger.info("✅ User ID {} authenticated and session registered: {}", userId, session.getId());
                    
                    // Envoyer confirmation de connexion
                    session.sendMessage(new TextMessage("{\"type\":\"CONNECTION_SUCCESS\",\"userId\":" + userId + "}"));
                } else {
                    logger.warn("❌ Invalid token, closing session {}", session.getId());
                    session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token invalide"));
                }
            } catch (Exception e) {
                logger.error("❌ Error validating token: {}", e.getMessage(), e);
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Erreur validation token: " + e.getMessage()));
            }
        } else {
            logger.warn("❌ Token missing, closing session {}", session.getId());
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token manquant"));
        }
    }
    
    private String extractTokenFromUri(java.net.URI uri) {
        if (uri == null) return null;
        
        String query = uri.getQuery();
        logger.info("🔍 Full query string: [{}]", query);
        
        if (query == null || query.isEmpty()) {
            return null;
        }
        
        // Méthode 1: Paramètre token=
        if (query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    String token = param.substring(6);
                    logger.info("🎯 Token found via parameter: [{}...]", token.length() > 10 ? token.substring(0, 10) : token);
                    return token;
                }
            }
        }
        
        // Méthode 2: Toute la query est le token (fallback)
        logger.info("🎯 Using entire query as token: [{}...]", query.length() > 10 ? query.substring(0, 10) : query);
        return query;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Received message: {} for session: {}", message.getPayload(), session.getId());

        Long senderId = (Long) session.getAttributes().get("userId");
        if (senderId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Utilisateur non authentifié"));
            return;
        }

        try {
            Map<String, Object> data = objectMapper.readValue(message.getPayload(), Map.class);
            Long annonceId = ((Number) data.get("annonceId")).longValue();
            String content = (String) data.get("content");

            // Récupérer le token depuis les attributs de session
            String token = (String) session.getAttributes().get("token");
            Message msg = messageService.sendMessageToAnnonceOwner(annonceId, content, senderId, token);
            session.sendMessage(new TextMessage("Message envoyé avec succès: " + msg.getId()));
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du message: {}", e.getMessage());
            session.sendMessage(new TextMessage("Erreur: " + e.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.removeSession(userId);
            logger.info("🔌 User {} session removed: {}", userId, session.getId());
        }
        logger.info("🔌 WebSocket connection closed for session: {} with status: {}", session.getId(), status);
    }
}

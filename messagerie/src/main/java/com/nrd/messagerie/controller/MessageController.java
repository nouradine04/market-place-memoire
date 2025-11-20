package com.nrd.messagerie.controller;

import com.nrd.messagerie.dto.MessageDto;
import com.nrd.messagerie.security.JwtUtil;
import com.nrd.messagerie.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    private final JwtUtil jwtUtil;
    private final com.nrd.messagerie.service.WebSocketSessionManager sessionManager;

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<MessageDto>> getConversationWith(
            @PathVariable Long otherUserId,
            @RequestHeader("Authorization") String token
    ) {
        Long currentUserId = jwtUtil.getUserIdFromToken(token.substring(7));
        List<MessageDto> messages = messageService.getConversationWith(currentUserId, otherUserId, token.substring(7));
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversation-partners")
    public ResponseEntity<List<Long>> getConversationPartners(
            @RequestHeader("Authorization") String token
    ) {
        Long currentUserId = jwtUtil.getUserIdFromToken(token.substring(7));
        List<Long> partners = messageService.getConversationPartners(currentUserId);
        return ResponseEntity.ok(partners);
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<MessageDto>> getMessageHistory(
            Authentication authentication
    ) {
        Long currentUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(messageService.getMessageHistory(currentUserId));
    }
    
    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(
            @RequestBody MessageDto messageDto,
            Authentication authentication
    ) {
        System.out.println("📬 Requête sendMessage reçue");
        System.out.println("👤 Authentication: " + authentication);
        System.out.println("📝 MessageDto: " + messageDto);
        
        try {
            Long currentUserId = (Long) authentication.getPrincipal();
            System.out.println("🎯 UserID extrait: " + currentUserId);
            
            messageDto.setSenderId(currentUserId);
            MessageDto savedMessage = messageService.sendMessage(messageDto);
            System.out.println("✅ Message envoyé avec succès !");
            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            System.err.println("❌ Erreur dans sendMessage: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/block/{userId}")
    public ResponseEntity<String> blockUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token
    ) {
        Long currentUserId = jwtUtil.getUserIdFromToken(token.substring(7));
        messageService.blockUser(currentUserId, userId);
        return ResponseEntity.ok("Utilisateur bloqué avec succès");
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Messagerie service fonctionne ! MongoDB: " + 
            (messageService != null ? "Connecté" : "Déconnecté"));
    }
    
    @GetMapping("/health")
    public ResponseEntity<Object> healthCheck() {
        return ResponseEntity.ok(java.util.Map.of(
            "status", "UP",
            "service", "messagerie-service",
            "port", 8083,
            "websocket", "ws://localhost:8083/ws?token=JWT_TOKEN",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
    
    @PostMapping("/debug-send")
    public ResponseEntity<Object> debugSend(
            @RequestBody java.util.Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            if (authHeader == null) {
                return ResponseEntity.badRequest().body("Header Authorization manquant");
            }
            
            if (!authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Format Authorization invalide, doit commencer par 'Bearer '");
            }
            
            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.validateToken(token);
            
            if (!isValid) {
                return ResponseEntity.badRequest().body("Token JWT invalide");
            }
            
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            return ResponseEntity.ok(java.util.Map.of(
                "message", "Token valide, prêt pour l'envoi",
                "userId", userId,
                "payload", payload,
                "authHeader", "Bearer [TOKEN_PRESENT]"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        // Pour l'instant retourner 0, à implémenter plus tard
        return ResponseEntity.ok(0);
    }
    
    @GetMapping("/websocket-status")
    public ResponseEntity<Object> getWebSocketStatus(Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        boolean isOnline = messageService.isUserOnline(currentUserId);
        
        return ResponseEntity.ok(java.util.Map.of(
            "userId", currentUserId,
            "isOnline", isOnline,
            "websocketUrl", "ws://localhost:8083/ws",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
    
    @PostMapping("/send")
    public ResponseEntity<MessageDto> sendMessageAlias(
            @RequestBody java.util.Map<String, Object> payload,
            Authentication authentication
    ) {
        System.out.println("📬 Payload reçu: " + payload);
        
        Long currentUserId = (Long) authentication.getPrincipal();
        
        // Vérification des champs requis
        Object receiverIdObj = payload.get("receiverId");
        Object annonceIdObj = payload.get("annonceId");
        Object contentObj = payload.get("content"); // Essayer 'content' au lieu de 'contenu'
        
        if (contentObj == null) {
            contentObj = payload.get("contenu"); // Fallback vers 'contenu'
        }
        
        if (receiverIdObj == null || annonceIdObj == null || contentObj == null) {
            throw new RuntimeException("Champs manquants: receiverId, annonceId, content");
        }
        
        MessageDto messageDto = MessageDto.builder()
                .senderId(currentUserId)
                .receiverId(Long.valueOf(receiverIdObj.toString()))
                .annonceId(Long.valueOf(annonceIdObj.toString()))
                .content(contentObj.toString())
                .build();
                
        return sendMessage(messageDto, authentication);
    }
    
    @GetMapping("/conversations")
    public ResponseEntity<Object> getConversationsWithDetails(Authentication authentication) {
        try {
            Long currentUserId = (Long) authentication.getPrincipal();
            Object result = messageService.getConversationSummary(currentUserId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Erreur conversations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(java.util.List.of()); // Retourner liste vide en cas d'erreur
        }
    }
    
    @GetMapping("/history/{conversationId}")
    public ResponseEntity<Object> getConversationHistory(
            @PathVariable String conversationId,
            Authentication authentication
    ) {
        try {
            Long currentUserId = (Long) authentication.getPrincipal();
            Long otherUserId = Long.valueOf(conversationId);
            
            // Récupérer le token depuis l'en-tête (si nécessaire)
            String token = "dummy-token"; // Pour l'instant
            
            List<MessageDto> messages = messageService.getConversationWith(currentUserId, otherUserId, token);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("Erreur récupération conversation: " + e.getMessage());
            return ResponseEntity.ok(java.util.List.of());
        }
    }

    @DeleteMapping("/block/{userId}")
    public ResponseEntity<String> unblockUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token
    ) {
        Long currentUserId = jwtUtil.getUserIdFromToken(token.substring(7));
        messageService.unblockUser(currentUserId, userId);
        return ResponseEntity.ok("Utilisateur débloqué avec succès");
    }
    
    @GetMapping("/debug-sessions")
    public ResponseEntity<Object> debugSessions(Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        boolean isOnline = messageService.isUserOnline(currentUserId);
        
        return ResponseEntity.ok(java.util.Map.of(
            "currentUserId", currentUserId,
            "isCurrentUserOnline", isOnline,
            "websocketUrl", "ws://localhost:8083/ws",
            "instructions", "Connectez-vous au WebSocket avec: ws://localhost:8083/ws?token=YOUR_JWT_TOKEN",
            "onlineUsers", sessionManager.getOnlineUsers()
        ));
    }
    
    @GetMapping("/debug-messages/{userId}")
    public ResponseEntity<Object> debugMessages(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        Long currentUserId = (Long) authentication.getPrincipal();
        
        try {
            List<com.nrd.messagerie.entity.Message> allMessages = messageService.getAllMessagesForUser(currentUserId);
            
            return ResponseEntity.ok(java.util.Map.of(
                "currentUserId", currentUserId,
                "targetUserId", userId,
                "totalMessages", allMessages.size(),
                "messages", allMessages.stream().limit(10).collect(java.util.stream.Collectors.toList())
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    
    @PostMapping("/test-websocket/{receiverId}")
    public ResponseEntity<String> testWebSocket(
            @PathVariable Long receiverId,
            @RequestBody java.util.Map<String, String> payload,
            Authentication authentication
    ) {
        Long currentUserId = (Long) authentication.getPrincipal();
        String testMessage = payload.getOrDefault("message", "Test WebSocket message");
        
        try {
            messageService.testWebSocketNotification(receiverId, currentUserId, testMessage);
            return ResponseEntity.ok("Message WebSocket de test envoyé à l'utilisateur " + receiverId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    
    @PostMapping("/debug-jwt")
    public ResponseEntity<Object> debugJwt(
            @RequestBody java.util.Map<String, String> payload
    ) {
        String token = payload.get("token");
        if (token == null) {
            return ResponseEntity.badRequest().body("Token manquant");
        }
        
        try {
            boolean isValid = jwtUtil.validateToken(token);
            if (isValid) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                return ResponseEntity.ok(java.util.Map.of(
                    "valid", true,
                    "userId", userId,
                    "role", role != null ? role : "N/A",
                    "tokenLength", token.length()
                ));
            } else {
                return ResponseEntity.ok(java.util.Map.of(
                    "valid", false,
                    "error", "Token invalide",
                    "tokenLength", token.length()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of(
                "valid", false,
                "error", e.getMessage(),
                "tokenLength", token.length()
            ));
        }
    }
}
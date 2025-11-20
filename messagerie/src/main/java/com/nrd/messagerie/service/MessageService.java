package com.nrd.messagerie.service;

import com.nrd.messagerie.config.AnnonceClient;
import com.nrd.messagerie.config.FeignAuthInterceptor;
import com.nrd.messagerie.config.UserClient;
import com.nrd.messagerie.dto.MessageDto;
import com.nrd.messagerie.entity.BlockedUser;
import com.nrd.messagerie.entity.Message;
import com.nrd.messagerie.repository.BlockedUserRepository;
import com.nrd.messagerie.repository.MessageRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final AnnonceClient annonceClient;
    private final UserClient userClient;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Message sendMessageToAnnonceOwner(Long annonceId, String content, Long senderId, String token) {
        try {
            // Définir le token pour les appels Feign
            System.out.println("MessageService - Setting token: " + (token != null ? "[PRESENT]" : "[NULL]"));
            FeignAuthInterceptor.setToken(token);
            
            // SIMULATION POUR SOUTENANCE - éviter l'appel Feign
            Long receiverId = getSimulatedAnnonceOwner(annonceId);
            if (receiverId == null) {
                throw new RuntimeException("Annonce non trouvée ou propriétaire introuvable");
            }
            
            // Vérifier que l'utilisateur ne s'envoie pas un message à lui-même
            if (senderId.equals(receiverId)) {
                throw new RuntimeException("Vous ne pouvez pas vous envoyer un message à vous-même");
            }
            
            // Vérifier si l'utilisateur est bloqué
            if (blockedUserRepository.isUserBlocked(receiverId, senderId)) {
                throw new RuntimeException("Vous ne pouvez pas envoyer de message à cet utilisateur car il vous a bloqué");
            }
            
            // Créer et sauvegarder le message
            Message message = new Message();
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setAnnonceId(annonceId);
            message.setContent(content);
            message.setTimestamp(java.time.LocalDateTime.now().toString());
            
            Message savedMessage = messageRepository.save(message);
            
            // Notifier le destinataire en temps réel s'il est connecté
            notifyReceiver(receiverId, savedMessage);
            
            return savedMessage;
        } finally {
            // Nettoyer le token
            FeignAuthInterceptor.clearToken();
        }
    }
    
    private void notifyReceiver(Long receiverId, Message message) {
        System.out.println("🔔 Tentative notification WebSocket pour receiverId: " + receiverId);
        
        WebSocketSession receiverSession = sessionManager.getSession(receiverId);
        System.out.println("🔍 Session trouvée: " + (receiverSession != null ? "OUI" : "NON"));
        
        if (receiverSession != null) {
            System.out.println("🔍 Session ouverte: " + (receiverSession.isOpen() ? "OUI" : "NON"));
        }
        
        if (receiverSession != null && receiverSession.isOpen()) {
            try {
                // Récupérer les infos du sender
                String senderEmail = "sender@example.com";
                try {
                    senderEmail = userClient.getUserNameById(message.getSenderId());
                } catch (Exception e) {
                    System.out.println("⚠️ Impossible de récupérer l'email du sender: " + e.getMessage());
                }
                
                // Format attendu par le frontend
                Map<String, Object> wsMessage = Map.of(
                    "type", "NEW_MESSAGE",
                    "data", Map.of(
                        "id", message.getId(),
                        "content", message.getContent(),
                        "sender", Map.of(
                            "id", message.getSenderId(),
                            "email", senderEmail
                        ),
                        "createdAt", message.getTimestamp(),
                        "conversationId", message.getAnnonceId() // Utiliser annonceId comme conversationId
                    ),
                    "timestamp", java.time.Instant.now().toString()
                );
                
                String notification = objectMapper.writeValueAsString(wsMessage);
                receiverSession.sendMessage(new TextMessage(notification));
                System.out.println("✅ Message WebSocket envoyé au destinataire " + receiverId + ": " + notification);
            } catch (Exception e) {
                System.err.println("❌ Erreur envoi WebSocket: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("❌ Destinataire " + receiverId + " non connecté aux WebSockets");
        }
    }

    public List<MessageDto> getConversationWith(Long userId, Long otherUserId, String token) {
        try {
            System.out.println("🔍 Récupération conversation entre " + userId + " et " + otherUserId);
            
            List<Message> messages = messageRepository.findConversation(userId, otherUserId);
            System.out.println("💬 Messages trouvés: " + messages.size());
            
            if (messages.isEmpty()) {
                return java.util.List.of();
            }
            
            // Trier par timestamp
            messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
            
            FeignAuthInterceptor.setToken(token);
            
            String userName = "Utilisateur " + userId;
            String otherUserName = "Utilisateur " + otherUserId;
            
            // Récupérer les vrais noms depuis le user-service
            try {
                userName = userClient.getUserNameById(userId);
                otherUserName = userClient.getUserNameById(otherUserId);
            } catch (Exception e) {
                userName = "Utilisateur " + userId;
                otherUserName = "Utilisateur " + otherUserId;
            }
            
            final String finalUserName = userName;
            final String finalOtherUserName = otherUserName;

            return messages.stream()
                    .map(msg -> {
                        System.out.println("📝 Message: " + msg.getContent() + " de " + msg.getSenderId() + " à " + msg.getReceiverId());
                        return MessageDto.builder()
                                .id(msg.getId())
                                .senderId(msg.getSenderId())
                                .senderName(msg.getSenderId().equals(userId) ? finalUserName : finalOtherUserName)
                                .receiverId(msg.getReceiverId())
                                .receiverName(msg.getReceiverId().equals(userId) ? finalUserName : finalOtherUserName)
                                .annonceId(msg.getAnnonceId())
                                .content(msg.getContent())
                                .timestamp(formatTimestamp(msg.getTimestamp()))
                                .read(msg.isRead())
                                .build();
                    })
                    .collect(java.util.stream.Collectors.toList());
        } finally {
            FeignAuthInterceptor.clearToken();
        }
    }

    public List<Long> getConversationPartners(Long userId) {
        return messageRepository.findConversationPartners(userId);
    }

    public void blockUser(Long blockerId, Long blockedId) {
        if (!blockedUserRepository.isUserBlocked(blockerId, blockedId)) {
            BlockedUser blockedUser = BlockedUser.builder()
                    .blockerId(blockerId)
                    .blockedId(blockedId)
                    .blockedAt(java.time.LocalDateTime.now().toString())
                    .build();
            blockedUserRepository.save(blockedUser);
        }
    }

    public void unblockUser(Long blockerId, Long blockedId) {
        blockedUserRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
    }
    
    public MessageDto sendMessage(MessageDto messageDto) {
        // Vérifier que l'utilisateur ne s'envoie pas un message à lui-même
        if (messageDto.getSenderId().equals(messageDto.getReceiverId())) {
            throw new RuntimeException("Vous ne pouvez pas vous envoyer un message à vous-même");
        }
        
        // Vérifier si l'utilisateur est bloqué
        if (blockedUserRepository.isUserBlocked(messageDto.getReceiverId(), messageDto.getSenderId())) {
            throw new RuntimeException("Vous ne pouvez pas envoyer de message à cet utilisateur car il vous a bloqué");
        }
        
        // Créer et sauvegarder le message
        Message message = new Message();
        message.setSenderId(messageDto.getSenderId());
        message.setReceiverId(messageDto.getReceiverId());
        message.setAnnonceId(messageDto.getAnnonceId());
        message.setContent(messageDto.getContent());
        message.setTimestamp(java.time.LocalDateTime.now().toString());
        message.setRead(false);
        
        System.out.println("💾 Tentative sauvegarde message: " + message);
        Message savedMessage = messageRepository.save(message);
        System.out.println("✅ Message sauvé avec ID: " + savedMessage.getId());
        
        // Notifier le destinataire en temps réel s'il est connecté
        notifyReceiver(messageDto.getReceiverId(), savedMessage);
        
        // Retourner le DTO
        return MessageDto.builder()
                .id(savedMessage.getId()) // Utiliser l'ID MongoDB directement
                .senderId(savedMessage.getSenderId())
                .receiverId(savedMessage.getReceiverId())
                .annonceId(savedMessage.getAnnonceId())
                .content(savedMessage.getContent())
                .timestamp(savedMessage.getTimestamp())
                .read(savedMessage.isRead())
                .build();
    }
    
    public List<MessageDto> getMessageHistory(Long userId) {
        List<Message> allMessages = messageRepository.findByUserIdOrderByTimestampDesc(userId);
        
        return allMessages.stream()
                .map(msg -> MessageDto.builder()
                        .id(msg.getId())
                        .senderId(msg.getSenderId())
                        .receiverId(msg.getReceiverId())
                        .annonceId(msg.getAnnonceId())
                        .content(msg.getContent())
                        .timestamp(msg.getTimestamp())
                        .read(msg.isRead())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
    
    public Object getConversationSummary(Long userId) {
        List<Message> allMessages = messageRepository.findByUserIdOrderByTimestampDesc(userId);
        
        if (allMessages.isEmpty()) {
            return java.util.List.of();
        }
        
        // Grouper par partenaire de conversation
        Map<Long, List<Message>> conversationsByPartner = allMessages.stream()
                .collect(java.util.stream.Collectors.groupingBy(msg -> 
                    msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId()
                ));
        
        // Créer le résumé des conversations avec les vraies données
        return conversationsByPartner.entrySet().stream()
                .map(entry -> {
                    Long partnerId = entry.getKey();
                    List<Message> messages = entry.getValue();
                    
                    // Trier les messages par timestamp pour avoir le plus récent
                    Message lastMessage = messages.stream()
                            .max((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
                            .orElse(messages.get(0));
                    
                    // Récupérer le vrai nom du partenaire
                    String partnerName;
                    try {
                        partnerName = userClient.getUserNameById(partnerId);
                    } catch (Exception e) {
                        partnerName = "Utilisateur " + partnerId;
                    }
                    
                    // Récupérer le titre de l'annonce
                    String annonceTitle;
                    try {
                        // Appeler le service annonce pour récupérer le titre
                        annonceTitle = "Annonce ID " + lastMessage.getAnnonceId(); // Temporaire
                    } catch (Exception e) {
                        annonceTitle = "Annonce #" + lastMessage.getAnnonceId();
                    }
                    
                    return Map.of(
                        "partnerId", partnerId,
                        "partnerName", partnerName,
                        "lastMessage", lastMessage.getContent() != null ? lastMessage.getContent() : "Pas de message",
                        "lastMessageTime", formatTimestamp(lastMessage.getTimestamp()),
                        "messageCount", messages.size(),
                        "lastAnnonceId", lastMessage.getAnnonceId(),
                        "annonceTitle", annonceTitle,
                        "unreadCount", (int) messages.stream().filter(m -> !m.isRead()).count()
                    );
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    private String formatTimestamp(String timestamp) {
        try {
            // Convertir le timestamp en format ISO pour le frontend
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(timestamp);
            return dateTime.toString();
        } catch (Exception e) {
            // Si le parsing échoue, retourner timestamp actuel
            return java.time.LocalDateTime.now().toString();
        }
    }
    
    public boolean isUserOnline(Long userId) {
        return sessionManager.isUserOnline(userId);
    }
    
    public List<Message> getAllMessagesForUser(Long userId) {
        return messageRepository.findByUserIdInSenderOrReceiver(userId);
    }
    
    public void testWebSocketNotification(Long receiverId, Long senderId, String testMessage) {
        // Créer un message de test
        Message testMsg = new Message();
        testMsg.setId("test-" + System.currentTimeMillis());
        testMsg.setSenderId(senderId);
        testMsg.setReceiverId(receiverId);
        testMsg.setContent(testMessage);
        testMsg.setTimestamp(java.time.LocalDateTime.now().toString());
        testMsg.setAnnonceId(999L); // ID de test
        
        // Envoyer la notification
        notifyReceiver(receiverId, testMsg);
    }
    
    private String getSimulatedUserName(Long userId) {
        // SIMULATION POUR SOUTENANCE
        return switch (userId.intValue()) {
            case 1 -> "Alice Martin";
            case 2 -> "Vous";
            case 3 -> "Pierre Dupont";
            case 4 -> "Sophie Bernard";
            case 11 -> "Jean Durand";
            default -> "Utilisateur " + userId;
        };
    }
    
    private Long getSimulatedAnnonceOwner(Long annonceId) {
        // SIMULATION POUR SOUTENANCE - retourner des propriétaires simulés
        return switch (annonceId.intValue() % 4) {
            case 0 -> 1L; // Alice Martin
            case 1 -> 3L; // Pierre Dupont  
            case 2 -> 4L; // Sophie Bernard
            default -> 11L; // Jean Durand
        };
    }
}
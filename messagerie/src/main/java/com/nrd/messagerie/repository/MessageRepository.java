package com.nrd.messagerie.repository;

import com.nrd.messagerie.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    
    @Query("{ $or: [ { 'senderId': ?0, 'receiverId': ?1 }, { 'senderId': ?1, 'receiverId': ?0 } ] }")
    List<Message> findConversation(Long userId1, Long userId2);
    
    // Méthode personnalisée pour éviter les problèmes de mapping
    default List<Long> findConversationPartners(Long userId) {
        List<Message> messages = findByUserIdInSenderOrReceiver(userId);
        return messages.stream()
                .map(msg -> msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Query("{ $or: [ { 'senderId': ?0 }, { 'receiverId': ?0 } ] }")
    List<Message> findByUserIdInSenderOrReceiver(Long userId);
    
    @Query(value = "{ $or: [ { 'senderId': ?0 }, { 'receiverId': ?0 } ] }", sort = "{ 'timestamp': -1 }")
    List<Message> findByUserIdOrderByTimestampDesc(Long userId);
}

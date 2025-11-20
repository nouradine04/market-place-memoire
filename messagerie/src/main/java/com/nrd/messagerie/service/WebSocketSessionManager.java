package com.nrd.messagerie.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class WebSocketSessionManager {
    
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    public void addSession(Long userId, WebSocketSession session) {
        userSessions.put(userId, session);
    }
    
    public void removeSession(Long userId) {
        userSessions.remove(userId);
    }
    
    public WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }
    
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }
    
    public java.util.Set<Long> getOnlineUsers() {
        return userSessions.keySet();
    }
}
package com.nrd.annonceservice.client;

import com.nrd.annonceservice.config.FeignAuthInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

// DTO pour la réponse utilisateur

@FeignClient(name = "user-service", url = "http://localhost:8081", configuration = FeignAuthInterceptor.class)
public interface UserClient {

    @PostMapping("/api/users/become-seller")
    String becomeSeller(); // Pas de userId, extrait du token

    @PostMapping("/api/users/block")
    String blockUser(); // À ajuster si besoin, mais pour l'instant, on garde
    
    @GetMapping("/api/users/{userId}/name")
    String getUserNameById(@PathVariable("userId") Long userId);
    
    @GetMapping("/api/users/{userId}/profile")
    Object getUserProfile(@PathVariable("userId") Long userId);
}
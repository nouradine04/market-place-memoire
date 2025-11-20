package com.nrd.annonceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "public-user-service", url = "http://localhost:8081")
public interface PublicUserClient {
    
    @GetMapping("/api/users/{userId}/public-profile")
    Object getPublicProfile(@PathVariable("userId") Long userId);
}
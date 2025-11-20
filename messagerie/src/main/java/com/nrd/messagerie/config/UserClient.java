package com.nrd.messagerie.config;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", configuration = FeignAuthInterceptor.class)
public interface UserClient {
    @GetMapping("/api/users/me")
    Long getCurrentUserId(@RequestHeader("Authorization") String authHeader);
    
    @GetMapping("/api/users/{id}/name")
    String getUserNameById(@PathVariable("id") Long id);
}

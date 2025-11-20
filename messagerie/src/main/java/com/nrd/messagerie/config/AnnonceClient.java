package com.nrd.messagerie.config;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "annonce-service", configuration = FeignAuthInterceptor.class)
public interface AnnonceClient {
    @GetMapping("/api/annonces/{id}/user")
    Long getUserIdByAnnonceId(@PathVariable("id") Long id);
}
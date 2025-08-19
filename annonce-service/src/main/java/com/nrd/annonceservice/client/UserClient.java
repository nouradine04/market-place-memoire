package com.nrd.annonceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "user-service")
public interface UserClient {

    @PostMapping("/api/users/become-seller/{id}")
    String becomeSeller(@PathVariable("id") Long id);
}
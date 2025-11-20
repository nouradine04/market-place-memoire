package com.nrd.messagerie.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();
    
    public static void setToken(String token) {
        tokenHolder.set(token);
    }
    
    public static void clearToken() {
        tokenHolder.remove();
    }

    @Override
    public void apply(RequestTemplate template) {
        String token = tokenHolder.get();
        System.out.println("FeignAuthInterceptor - Token: " + (token != null ? "[PRESENT]" : "[NULL]"));
        if (token != null && !token.isEmpty()) {
            template.header("Authorization", "Bearer " + token);
            System.out.println("FeignAuthInterceptor - Authorization header added");
        }
    }
}
//package com.nrd.annonceservice.client;
//
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@FeignClient(name = "ia-service") // Enregistré dans Eureka
//public interface IaClient {
//
//    @PostMapping("/api/analyze")
//    String analyzeImage(@RequestBody Long annonceId); // Ou List<String> urls si besoin
//}
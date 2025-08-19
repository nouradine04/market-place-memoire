package com.nrd.annonceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient  // Si pas déjà
@EnableFeignClients
public class AnnonceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnnonceServiceApplication.class, args);
	}

}

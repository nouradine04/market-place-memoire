package com.nrd.annonceservice;

import com.nrd.annonceservice.entity.Categorie;
import com.nrd.annonceservice.repository.CategorieRepository;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.crypto.SecretKey;
import java.util.Base64;

@SpringBootApplication
@EnableScheduling

@EnableFeignClients
public class AnnonceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnnonceServiceApplication.class, args);

		SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
		String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
		System.out.println(base64Key); // Copie
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	};

	@Bean
	CommandLineRunner initCategories(CategorieRepository categorieRepository) {
		return args -> {
			if (categorieRepository.count() == 0) {
				categorieRepository.save(new Categorie(null, "Electroménager"));
				categorieRepository.save(new Categorie(null, "Immobilier"));
				categorieRepository.save(new Categorie(null, "Informatique"));
				categorieRepository.save(new Categorie(null, "Véhicules"));
				categorieRepository.save(new Categorie(null, "Mode & Beauté"));
				System.out.println("Catégories initialisées !");
			}};


		}
	}
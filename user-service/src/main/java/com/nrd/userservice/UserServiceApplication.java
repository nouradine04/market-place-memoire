package com.nrd.userservice;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.nrd.userservice.entity.User;
import com.nrd.userservice.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication

public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);

//		SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
//		String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
//		System.out.println(base64Key); // Copie
	}


	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	CommandLineRunner createAdmin(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, FirebaseAuth firebaseAuth) {
		return args -> {
			System.out.println("🚀 CommandLineRunner démarré...");
			try {
				boolean adminExists = userRepository.existsByEmail("nourad@gmail.com");
				System.out.println("🔍 Admin existe déjà: " + adminExists);
				
				if (!adminExists) {
					System.out.println("📝 Création admin en cours...");
					
					UserRecord.CreateRequest request = new UserRecord.CreateRequest()
							.setEmail("nourad@gmail.com")
							.setPassword("admin123")
							.setDisplayName("Admin System");
					
					UserRecord firebaseUser = firebaseAuth.createUser(request);
					System.out.println("🔥 Firebase user créé: " + firebaseUser.getUid());
					
					User admin = User.builder()
							.email("nourad@gmail.com")
							.firebaseUid(firebaseUser.getUid())
							.password(passwordEncoder.encode("admin123"))
							.prenom("Admin")
							.telephone("65719407")
							.nom("System")
							.role(User.Role.ADMIN)
							.build();
					
					userRepository.save(admin);
					System.out.println("✅ Admin créé: nourad@gmail.com / admin123");
				} else {
					System.out.println("ℹ️ Admin existe déjà");
				}
			} catch (Exception e) {
				System.err.println("❌ Erreur création admin: " + e.getMessage());
				e.printStackTrace();
			}
		};
	}


}

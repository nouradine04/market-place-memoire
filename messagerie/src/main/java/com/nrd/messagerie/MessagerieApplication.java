package com.nrd.messagerie;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import javax.crypto.SecretKey;
import java.util.Base64;

@SpringBootApplication
@EnableFeignClients
public class MessagerieApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagerieApplication.class, args);

//        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
//        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
//        System.out.println(base64Key);
//
//        System.out.println("\n🚀 ===== MESSAGERIE SERVICE DÉMARRÉ =====\n");
//        System.out.println("🌐 Service URL: http://localhost:8083");
//        System.out.println("🔌 WebSocket URL: ws://localhost:8083/ws");
//        System.out.println("📝 API Endpoints:");
//        System.out.println("   POST /api/messages/send - Envoyer un message");
//        System.out.println("   GET  /api/messages/websocket-status - Vérifier statut WebSocket");
//        System.out.println("   POST /api/messages/test-websocket/{receiverId} - Tester WebSocket");
//        System.out.println("\n🔍 Pour tester: Connectez-vous au WebSocket avec un token JWT valide\n");
    }

}

package com.nrd.userservice.controller;

import com.nrd.userservice.dto.*;
import com.nrd.userservice.entity.User;
import com.nrd.userservice.exception.CustomException;
import com.nrd.userservice.security.JwtUtil;
import com.nrd.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody UserRegistrationDto dto) {
        return ResponseEntity.ok(userService.register(dto));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        UserResponseDto userDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/become-seller")
    public ResponseEntity<String> becomeSeller(@RequestHeader("Authorization") String authHeader) {
        log.info("Requête reçue pour /become-seller avec header: {}", authHeader);
        String token = authHeader.substring(7);
        Long userId = null;
        try {
            userId = jwtUtil.getUserIdFromToken(token);
//            log.info("User ID extrait du token : {}", userId);
        } catch (Exception e) {
//            log.error("Erreur lors de l'extraction du userId : {}", e.getMessage());
            throw new CustomException("Token invalide ");
        }

        try {
            userService.becomeSeller(userId);
//            log.info("Promotion reussi pour l'utilisateur {}", userId);
            return ResponseEntity.ok("Promu vendeur avec succès");
        } catch (Exception e) {
//            log.error("Erreur lors de la promotion de l'utilisateur  {}", e.getMessage());
            throw new CustomException("Erreur lors de la promotion en vendeur : " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserLoginDto dto) {
        return ResponseEntity.ok(userService.login(dto.getIdentifier(), dto.getPassword()));
    }
    
    @PostMapping("/oauth/google")
    public ResponseEntity<String> googleOAuth(@RequestBody java.util.Map<String, String> request) {
        String idToken = request.get("idToken");
        return ResponseEntity.ok(userService.authenticateWithGoogle(idToken));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponseDto> updateProfile(@RequestBody UserProfileUpdateDto dto, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        return ResponseEntity.ok(userService.updateProfile(userId, dto));
    }

    // Admin endpoints
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/admin/block/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> blockUser(@PathVariable Long userId) {
        userService.blockUser(userId);
        return ResponseEntity.ok("User bloqué");
    }

    @PutMapping("/admin/promote/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> promoteToAdmin(@PathVariable Long userId) {
        userService.promoteToAdmin(userId);
        return ResponseEntity.ok("Promu admin");
    }

    @GetMapping("/admin/stats/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getTotalUsers() {
        return ResponseEntity.ok(userService.getTotalUsers());
    }

    // -------------------- Endpoints publics --------------------
    @GetMapping("/{userId}/name")
    public ResponseEntity<String> getUserName(@PathVariable Long userId) {
        String userName = userService.getUserName(userId);
        return ResponseEntity.ok(userName);
    }
    
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserResponseDto> getUserProfileById(@PathVariable Long userId) {
        UserResponseDto userDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(userDto);
    }
    
    @GetMapping("/{userId}/seller-info")
    public ResponseEntity<Object> getSellerInfo(@PathVariable Long userId) {
        UserResponseDto user = userService.getUserProfile(userId);
        return ResponseEntity.ok(java.util.Map.of(
            "nom", user.getNom() != null ? user.getNom() : "",
            "prenom", user.getPrenom() != null ? user.getPrenom() : "",
            "telephone", user.getTelephone() != null ? user.getTelephone() : "",
            "email", user.getEmail()
        ));
    }
    
    @GetMapping("/{userId}/details")
    public ResponseEntity<Object> getUserDetails(@PathVariable Long userId) {
        try {
            UserResponseDto user = userService.getUserProfile(userId);
            
            // Extraire les réseaux sociaux du Map
            java.util.Map<String, String> socialNetworks = user.getSocialNetworks();
            String facebook = socialNetworks != null ? socialNetworks.get("facebook") : "";
            String instagram = socialNetworks != null ? socialNetworks.get("instagram") : "";
            String telegram = socialNetworks != null ? socialNetworks.get("telegram") : "";
            String tiktok = socialNetworks != null ? socialNetworks.get("tiktok") : "";
            String whatsapp = socialNetworks != null ? socialNetworks.get("whatsapp") : "";
            
            return ResponseEntity.ok(java.util.Map.of(
                "id", user.getId(),
                "nom", user.getNom() != null ? user.getNom() : "",
                "prenom", user.getPrenom() != null ? user.getPrenom() : "",
                "telephone", user.getTelephone() != null ? user.getTelephone() : "",
                "email", user.getEmail(),
                "facebook", facebook != null ? facebook : "",
                "instagram", instagram != null ? instagram : "",
                "telegram", telegram != null ? telegram : "",
                "tiktok", tiktok != null ? tiktok : "",
                "whatsapp", whatsapp != null ? whatsapp : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }
    
    // -------------------- Endpoint public pour visiteurs non connectés --------------------
    @GetMapping("/{userId}/public-profile")
    public ResponseEntity<Object> getPublicProfile(@PathVariable Long userId) {
        try {
            UserResponseDto user = userService.getUserProfile(userId);
            
            // Extraire les réseaux sociaux
            java.util.Map<String, String> socialNetworks = user.getSocialNetworks();
            
            return ResponseEntity.ok(java.util.Map.of(
                "nom", user.getNom() != null ? user.getNom() : "",
                "prenom", user.getPrenom() != null ? user.getPrenom() : "",
                "telephone", user.getTelephone() != null ? user.getTelephone() : "",
                "email", user.getEmail(),
                "socialNetworks", socialNetworks != null ? socialNetworks : java.util.Map.of()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of(
                "nom", "Utilisateur " + userId,
                "prenom", "",
                "telephone", "",
                "email", "",
                "socialNetworks", java.util.Map.of()
            ));
        }
    }
}
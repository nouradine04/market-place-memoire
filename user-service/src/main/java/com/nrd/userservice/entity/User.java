package com.nrd.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String firebaseUid; // liason firebaase
    private String telephone;
    private String password;
    private String prenom;
    private String nom;
    private String avatar;
    @ElementCollection
    private Map<String, String> socialNetworks;
    @Builder.Default
    private boolean vendeur = false; // true a la  premiere annonce
    @Builder.Default
    private boolean estBloque = false;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // Enum Role
    public enum Role {
        USER, ADMIN
    }

    // Méthode pour setter le password (déjà hashé)
    public void setPassword(String password) {
        this.password = password;
    }
}

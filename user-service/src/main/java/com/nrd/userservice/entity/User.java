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
    private boolean isVendeur = false; // true a la  premiere annonce
    private boolean estBloque = false;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;  // USER par défaut, ADMIN pour admins

    // Enum Role
    public enum Role {
        USER, ADMIN
    }

    // Méthode pour hasher
    public void setPassword(String password) {
        this.password = new BCryptPasswordEncoder().encode(password);
    }
}

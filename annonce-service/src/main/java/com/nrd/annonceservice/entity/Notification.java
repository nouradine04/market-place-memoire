package com.nrd.annonceservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private Long annonceId;
    private String titre;
    private String message;
    private String type; // APPROVED, REJECTED, EXPIRED
    
    @Builder.Default
    private boolean lu = false;
    
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();
}
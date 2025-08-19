package com.nrd.annonceservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signalements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Signalement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long annonceId;
    private Long userId;  // Qui signale
    private String raison;
    private LocalDateTime dateSignalement = LocalDateTime.now();
    private String description;
}
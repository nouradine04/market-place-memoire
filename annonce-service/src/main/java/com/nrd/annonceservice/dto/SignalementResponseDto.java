package com.nrd.annonceservice.dto;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SignalementResponseDto {
    private Long id;               // ID du signalement
    private Long annonceId;        // ID de l'annonce signalée
    private Long userId;           // ID de l'utilisateur qui signale
    private String raison;         // Raison choisie (combo box)
    private String description;    // Description libre
    private LocalDateTime dateSignalement; // Date du signalement
}

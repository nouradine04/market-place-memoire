package com.nrd.annonceservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnnonceResponseDto {
    private Long id;
    private String titre;
    private String description;
    private double prix;
    private String devise;
    private Long userId;
    private String categorieNom;
    private List<String> images;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    private String statut;
    private int vues;
    private int messages;
}
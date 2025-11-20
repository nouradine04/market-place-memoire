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
    private int nombreSignalements;
    private String ville;
    private String adresse;
    private String telephone;
    private LocalDateTime datePublication;
    private boolean isOwner = false; // Pour savoir si c'est son annonce

}
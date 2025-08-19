package com.nrd.annonceservice.dto;


import lombok.Data;

@Data
public class CategorieResponseDto {
    private Long id;     // ID de la catégorie
    private String nom;  // Nom de la catégorie (ex: "Véhicules")
}


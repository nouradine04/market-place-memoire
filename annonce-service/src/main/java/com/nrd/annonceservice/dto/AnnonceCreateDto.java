package com.nrd.annonceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class AnnonceCreateDto {
    @NotBlank
    private String titre;
    @NotBlank
    private String description;
    @Positive
    private double prix;
    private Long categorieId;
    private List<String> images; // URLs des images (pas MultipartFile)
    @NotBlank(message = "Ville requise")
    private String ville;
}
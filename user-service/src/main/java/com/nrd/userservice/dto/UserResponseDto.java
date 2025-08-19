package com.nrd.userservice.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String telephone;
    private String prenom;
    private String nom;
    private String avatar;
    private Map<String, String> socialNetworks; // Seulement si isVendeur
    private boolean isVendeur;
}
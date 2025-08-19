package com.nrd.userservice.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "Nom requis")
    private String nom;
    @NotBlank(message = "Prénom requis")
    private String prenom;
    private String email;
    private String telephone;
    @NotBlank(message = "Mot de passe requis")
    @Size(min = 6, message = "Mot de passe trop court")
    private String password;
    private Map<String, String> socialNetworks; // Optionnel à inscription


}

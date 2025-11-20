package com.nrd.userservice.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class UserProfileUpdateDto {
    private String prenom;
    private String nom;
    private String avatar;
    @NotBlank(message = "Réseaux requis pour vendeurs")
    private Map<String, String> socialNetworks;
}
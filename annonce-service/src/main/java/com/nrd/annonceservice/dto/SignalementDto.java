package com.nrd.annonceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignalementDto {
    @NotNull
    private Long userId;

    @NotBlank
    private String raison;

    private String description;
}

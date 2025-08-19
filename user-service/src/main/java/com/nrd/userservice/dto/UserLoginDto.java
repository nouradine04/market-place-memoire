package com.nrd.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDto {
    @NotBlank
    private String identifier;
    @NotBlank
    private String password;
}

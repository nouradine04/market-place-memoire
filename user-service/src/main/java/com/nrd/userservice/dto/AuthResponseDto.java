package com.nrd.userservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
public class AuthResponseDto {
    private UserResponseDto user;
    private String token;

    public AuthResponseDto(UserResponseDto user, String token) {
        this.user = user;
        this.token = token;
    }
}


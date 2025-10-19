package com.example.bookingservice.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class CreateUserRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String role;
}

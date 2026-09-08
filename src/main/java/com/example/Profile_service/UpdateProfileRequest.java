package com.example.Profile_service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank
        String name,

        @NotBlank
        @Email
        String email,

        @Size(max = 500)
        String bio,

        @NotBlank
        String address
) {
}
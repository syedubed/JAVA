package com.example.Profile_service.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
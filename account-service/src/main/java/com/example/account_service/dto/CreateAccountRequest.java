package com.example.account_service.dto;

import com.example.account_service.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(

        @NotNull(message = "Profile ID is required")
        @Positive(message = "Profile ID must be positive")
        Long profileId,

        @NotBlank(message = "Account number is required")
        @Size(max = 30, message = "Account number must not exceed 30 characters")
        String accountNumber,

        @NotNull(message = "Account type is required")
        AccountType accountType
) {
}
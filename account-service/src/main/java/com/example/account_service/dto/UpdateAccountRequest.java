package com.example.account_service.dto;

import com.example.account_service.model.AccountStatus;
import com.example.account_service.model.AccountType;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountRequest(

        @NotNull(message = "Account type is required")
        AccountType accountType,

        @NotNull(message = "Account status is required")
        AccountStatus status
) {
}
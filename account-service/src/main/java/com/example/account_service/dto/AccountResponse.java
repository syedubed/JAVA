package com.example.account_service.dto;

import com.example.account_service.model.AccountStatus;
import com.example.account_service.model.AccountType;

public record AccountResponse(
        Long id,
        Long profileId,
        String accountNumber,
        AccountType accountType,
        AccountStatus status
) {
}

package com.example.account_service.model;

public record Account(
        Long id,
        Long profileId,
        String accountNumber,
        AccountType accountType,
        AccountStatus status
) {
}
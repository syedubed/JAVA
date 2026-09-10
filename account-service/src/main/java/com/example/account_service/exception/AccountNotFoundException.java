package com.example.account_service.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long id) {
        super("Account was not found with ID: " + id);
    }
}
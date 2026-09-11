package com.example.account_service.exception;

public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(Long profileId) {
        super("Profile was not found with ID: " + profileId);
    }
}
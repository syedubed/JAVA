package com.example.account_service.exception;

public class ProfileServiceUnavailableException
        extends RuntimeException {

    public ProfileServiceUnavailableException(
            Throwable cause
    ) {
        super(
                "Profile Service is currently unavailable",
                cause
        );
    }
}
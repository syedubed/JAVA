package com.example.account_service.client;

import com.example.account_service.dto.ProfileResponse;

public interface ProfileClient {

    ProfileResponse getProfile(
            Long profileId,
            String authorizationHeader,
            String correlationId
    );
}
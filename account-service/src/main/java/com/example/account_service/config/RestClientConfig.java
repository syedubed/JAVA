package com.example.account_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean("profileRestClient")
    public RestClient profileRestClient(
            RestClient.Builder builder,
            @Value("${profile-service.base-url}")
            String profileServiceBaseUrl
    ) {
        return builder
                .baseUrl(profileServiceBaseUrl)
                .build();
    }
}
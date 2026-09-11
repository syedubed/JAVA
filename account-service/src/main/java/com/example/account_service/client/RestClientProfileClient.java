package com.example.account_service.client;

import com.example.account_service.dto.ProfileResponse;
import com.example.account_service.exception.ProfileNotFoundException;
import com.example.account_service.exception.ProfileServiceUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class RestClientProfileClient implements ProfileClient {

    private static final String CORRELATION_ID_HEADER =
            "X-Correlation-ID";

    private final RestClient restClient;

    public RestClientProfileClient(
            @Qualifier("profileRestClient")
            RestClient restClient
    ) {
        this.restClient = restClient;
    }

    @Override
    public ProfileResponse getProfile(
            Long profileId,
            String authorizationHeader,
            String correlationId
    ) {
        try {
            return restClient.get()
                    .uri("/api/profiles/{id}", profileId)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            authorizationHeader
                    )
                    .header(
                            CORRELATION_ID_HEADER,
                            correlationId
                    )
                    .retrieve()
                    .body(ProfileResponse.class);

        } catch (HttpClientErrorException.NotFound exception) {
            throw new ProfileNotFoundException(profileId);

        } catch (ResourceAccessException exception) {
            throw new ProfileServiceUnavailableException(
                    exception
            );
        }
    }

}
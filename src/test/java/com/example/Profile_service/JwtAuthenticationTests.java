package com.example.Profile_service;

import com.example.Profile_service.dto.TokenResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
class JwtAuthenticationTests {

    @TempDir
    static Path temporaryDirectory;

    @DynamicPropertySource
    static void configureStorage(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "profiles.storage.file",
                () -> temporaryDirectory
                        .resolve("profiles.json")
                        .toString()
        );
    }

    @Autowired
    private RestTestClient client;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    /*
     * Reusable helper for tests that need a valid token.
     */
    private String loginAndGetToken() {
        TokenResponse response = client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                      "username": "admin",
                      "password": "password"
                    }
                    """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertNotNull(response.accessToken());

        return response.accessToken();
    }

    /*
     * Successful authentication should return a JWT.
     */
    @Test
    void shouldReturnTokenForValidCredentials() {
        TokenResponse response = client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                      "username": "admin",
                      "password": "password"
                    }
                    """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertFalse(response.accessToken().isBlank());
        assertEquals("Bearer", response.tokenType());
        assertTrue(response.expiresIn() > 0);

        // A JWT must contain header, payload and signature.
        assertEquals(
                3,
                response.accessToken().split("\\.").length
        );
    }

    /*
     * Incorrect credentials should not produce a JWT.
     */
    @Test
    void shouldRejectInvalidPassword() {
        client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                      "username": "admin",
                      "password": "incorrect-password"
                    }
                    """)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /*
     * Request validation should reject blank credentials.
     */
    @Test
    void shouldRejectBlankCredentials() {
        client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                      "username": "",
                      "password": ""
                    }
                    """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    /*
     * A protected profile endpoint requires authentication.
     */
    @Test
    void shouldRejectProfileRequestWithoutToken() {
        client.get()
                .uri("/api/profiles")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /*
     * A valid JWT should allow access.
     */
    @Test
    void shouldAllowProfileRequestWithValidToken() {
        String token = loginAndGetToken();

        client.get()
                .uri("/api/profiles")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .exchange()
                .expectStatus().isOk();
    }

    /*
     * A malformed token should fail signature/format validation.
     */
    @Test
    void shouldRejectInvalidToken() {
        client.get()
                .uri("/api/profiles")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer invalid.jwt.token"
                )
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /*
     * An expired, correctly signed token must still be rejected.
     */
    @Test
    void shouldRejectExpiredToken() {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("profile-service")
                .subject("admin")
                .issuedAt(now.minusSeconds(120))
                .expiresAt(now.minusSeconds(60))
                .claim("scope", "ROLE_USER")
                .build();

        String expiredToken = jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        client.get()
                .uri("/api/profiles")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + expiredToken
                )
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /*
     * Verify that our generated JWT contains the expected claims.
     */
    @Test
    void shouldGenerateExpectedTokenClaims() {
        String token = loginAndGetToken();

        Jwt decodedToken = jwtDecoder.decode(token);

        assertEquals("admin", decodedToken.getSubject());
        assertEquals(
                "profile-service",
                decodedToken.getClaimAsString("iss")
        );
        assertEquals(
                "ROLE_USER",
                decodedToken.getClaimAsString("scope")
        );
        assertNotNull(decodedToken.getIssuedAt());
        assertNotNull(decodedToken.getExpiresAt());
    }

    /*
     * Swagger documentation should remain public.
     */
    @Test
    void shouldAllowOpenApiDocumentationWithoutToken() {
        client.get()
                .uri("/v3/api-docs")
                .exchange()
                .expectStatus().isOk();
    }
}
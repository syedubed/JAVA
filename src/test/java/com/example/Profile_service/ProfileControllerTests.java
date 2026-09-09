package com.example.Profile_service;

import com.example.Profile_service.model.Profile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.example.Profile_service.dto.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpHeaders;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
class ProfileControllerTests {

    @TempDir
    static Path temporaryDirectory;

    @DynamicPropertySource
    static void configureStorage(DynamicPropertyRegistry registry) {
        registry.add(
                "profiles.storage.file",
                () -> temporaryDirectory.resolve("profiles.json").toString()
        );
    }

    @Autowired
    private RestTestClient client;

    @Test
    void shouldCreateProfile() {
        // ARRANGE: prepare the input.
        String requestBody = """
                {
                  "name": "Ubaid",
                  "email": "ubaid@example.com",
                  "bio": "Learning automated testing",
                   "address": "Phoenix, Arizona",
                   "mobileNumber": "+16025550123"
                }
                """;

        // ACT: send the request.
        client.post()
                .uri("/api/profiles")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange()

                // ASSERT: check the response.
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.name").isEqualTo("Ubaid")
                .jsonPath("$.email").isEqualTo("ubaid@example.com")
                .jsonPath("$.bio")
                .isEqualTo("Learning automated testing")
                .jsonPath("$.address")
                .isEqualTo("Phoenix, Arizona")
                .jsonPath("$.mobileNumber")
                .isEqualTo("+16025550123");
    }

    @Test
    void shouldRejectBlankName() {
        client.post()
                .uri("/api/profiles")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                      "name": "",
                      "email": "ubaid@example.com",
                      "bio": "Testing validation",
                            "address": "Phoenix, Arizona",
                                "mobileNumber": "+16025550123"
                    }
                    """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    //test case for the PUT Method

    @Test
    void shouldUpdateExistingProfile() {
        // ARRANGE: create a profile for this test.
        Profile created = client.post()
                .uri("/api/profiles")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                      "name": "Alex",
                      "email": "alex@example.com",
                      "bio": "Original bio",
                          "address": "Phoenix, Arizona",
                          "mobileNumber": "+16025550123"
                      
                    }
                    """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Profile.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(created);
        assertNotNull(created.id());

        // Define what the updated profile should contain.
        Profile expected = new Profile(
                created.id(),
                "Alex Updated",
                "updated@example.com",
                "Updated bio",
                "Tempe, Arizona",
                created.mobileNumber()
        );

        // ACT: update the existing profile.
        client.put()
                .uri("/api/profiles/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .header(
                        "X-Correlation-ID","test-put-101"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                      "name": "Alex Updated",
                      "email": "updated@example.com",
                      "bio": "Updated bio",
                        "address": "Tempe, Arizona"
                    }
                    """)
                .exchange()

                // ASSERT: verify the PUT response.
                .expectStatus().isOk()
                .expectBody(Profile.class)
                .isEqualTo(expected);

        // Verify the changes were actually saved.
        client.get()
                .uri("/api/profiles/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Profile.class)
                .isEqualTo(expected);
    }

    // Testing the delete Method

    @Test
    void shouldDeleteExistingProfile() {
        // ARRANGE: create a profile to delete.
        Profile created = client.post()
                .uri("/api/profiles")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                      "name": "Alex",
                      "email": "alex@example.com",
                      "bio": "Profile to delete",
                      "address": "Phoenix, Arizona",
                      "mobileNumber": "+16025550123"
                    }
                    """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Profile.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(created);
        assertNotNull(created.id());

        // ACT: delete the created profile.
        client.delete()
                .uri("/api/profiles/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()

                // ASSERT: successful deletion returns no content.
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        // Verify that the profile no longer exists.
        client.get()
                .uri("/api/profiles/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()
                .expectStatus().isNotFound();
    }

    // PROFILE DOESNT EXIST CASES

    @Test
    void shouldReturn404WhenGettingMissingProfile() {
        client.get()
                .uri("/api/profiles/{id}", -1L)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn404WhenUpdatingMissingProfile() {
        client.put()
                .uri("/api/profiles/{id}", -1L)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                      "name": "Alex",
                      "email": "alex@example.com",
                      "bio": "This profile does not exist",
                      "address": "Phoenix, Arizona"
                    }
                    """)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn404WhenDeletingMissingProfile() {
        client.delete()
                .uri("/api/profiles/{id}", -1L)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()
                .expectStatus().isNotFound();
    }


    private String authorizationHeader;

    @BeforeEach
    void authenticate() {
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

        authorizationHeader =
                "Bearer " + response.accessToken();
    }
}

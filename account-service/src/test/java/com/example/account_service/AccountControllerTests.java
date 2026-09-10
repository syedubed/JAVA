package com.example.account_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
class AccountControllerTests {

    @Autowired
    private RestTestClient client;

    @Test
    void shouldCreateAccount() {
        String requestBody = """
                {
                  "profileId": 2,
                  "accountNumber": "ACC-CREATE-10001",
                  "accountType": "CREDIT_CARD"
                }
                """;

        client.post()
                .uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.profileId").isEqualTo(2)
                .jsonPath("$.accountNumber")
                .isEqualTo("ACC-CREATE-10001")
                .jsonPath("$.accountType")
                .isEqualTo("CREDIT_CARD")
                .jsonPath("$.status")
                .isEqualTo("ACTIVE");
    }

    @Test
    void shouldRejectBlankAccountNumber() {
        client.post()
                .uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "profileId": 2,
                          "accountNumber": "",
                          "accountType": "CREDIT_CARD"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message")
                .isEqualTo("Request validation failed")
                .jsonPath("$.fieldErrors.accountNumber")
                .isEqualTo("Account number is required");
    }

    @Test
    void shouldRejectDuplicateAccountNumber() {
        String requestBody = """
                {
                  "profileId": 2,
                  "accountNumber": "ACC-DUPLICATE-10001",
                  "accountType": "CREDIT_CARD"
                }
                """;

        client.post()
                .uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange()
                .expectStatus().isCreated();

        client.post()
                .uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.error").isEqualTo("Conflict")
                .jsonPath("$.message")
                .isEqualTo(
                        "Account number already exists: " +
                                "ACC-DUPLICATE-10001"
                );
    }
}
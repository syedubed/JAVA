package com.example.account_service;

import com.example.account_service.dto.AccountResponse;
import com.example.account_service.dto.CreateAccountRequest;
import com.example.account_service.model.AccountType;
import com.example.account_service.repository.InMemoryAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
class AccountControllerTests {

    @Autowired
    private RestTestClient client;

    @Autowired
    private InMemoryAccountRepository accountRepository;

    @BeforeEach
    void clearAccounts() {
        accountRepository.deleteAll();
    }

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

    @Test
    void shouldGetExistingAccount() {
        AccountResponse created = createAccount(
                2L,
                "ACC-GET-10001",
                AccountType.CREDIT_CARD
        );

        client.get()
                .uri("/api/accounts/{id}", created.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(created.id())
                .jsonPath("$.profileId").isEqualTo(2)
                .jsonPath("$.accountNumber")
                .isEqualTo("ACC-GET-10001")
                .jsonPath("$.accountType")
                .isEqualTo("CREDIT_CARD")
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    void shouldReturn404WhenGettingMissingAccount() {
        client.get()
                .uri("/api/accounts/{id}", 999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message")
                .isEqualTo("Account was not found with ID: 999")
                .jsonPath("$.path")
                .isEqualTo("/api/accounts/999");
    }

    @Test
    void shouldGetAllAccounts() {
        createAccount(
                2L,
                "ACC-LIST-10001",
                AccountType.CREDIT_CARD
        );
        createAccount(
                3L,
                "ACC-LIST-10002",
                AccountType.SAVINGS
        );

        client.get()
                .uri("/api/accounts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].accountNumber")
                .isEqualTo("ACC-LIST-10001")
                .jsonPath("$[1].accountNumber")
                .isEqualTo("ACC-LIST-10002");
    }

    @Test
    void shouldReturnEmptyAccountList() {
        client.get()
                .uri("/api/accounts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void shouldUpdateExistingAccount() {
        AccountResponse created = createAccount(
                2L,
                "ACC-UPDATE-10001",
                AccountType.CREDIT_CARD
        );

        client.put()
                .uri("/api/accounts/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "accountType": "CHECKING",
                          "status": "BLOCKED"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(created.id())
                .jsonPath("$.profileId").isEqualTo(2)
                .jsonPath("$.accountNumber")
                .isEqualTo("ACC-UPDATE-10001")
                .jsonPath("$.accountType")
                .isEqualTo("CHECKING")
                .jsonPath("$.status").isEqualTo("BLOCKED");
    }

    @Test
    void shouldReturn404WhenUpdatingMissingAccount() {
        client.put()
                .uri("/api/accounts/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "accountType": "CHECKING",
                          "status": "ACTIVE"
                        }
                        """)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message")
                .isEqualTo("Account was not found with ID: 999");
    }

    @Test
    void shouldRejectInvalidAccountUpdate() {
        AccountResponse created = createAccount(
                2L,
                "ACC-INVALID-UPDATE-10001",
                AccountType.CREDIT_CARD
        );

        client.put()
                .uri("/api/accounts/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "accountType": "CHECKING",
                          "status": null
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.fieldErrors.status")
                .isEqualTo("Account status is required");
    }

    @Test
    void shouldDeleteExistingAccount() {
        AccountResponse created = createAccount(
                2L,
                "ACC-DELETE-10001",
                AccountType.CREDIT_CARD
        );

        client.delete()
                .uri("/api/accounts/{id}", created.id())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        client.get()
                .uri("/api/accounts/{id}", created.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn404WhenDeletingMissingAccount() {
        client.delete()
                .uri("/api/accounts/{id}", 999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message")
                .isEqualTo("Account was not found with ID: 999");
    }

    private AccountResponse createAccount(
            Long profileId,
            String accountNumber,
            AccountType accountType
    ) {
        AccountResponse response = client.post()
                .uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateAccountRequest(
                        profileId,
                        accountNumber,
                        accountType
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AccountResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        return response;
    }
}

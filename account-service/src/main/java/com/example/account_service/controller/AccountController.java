package com.example.account_service.controller;

import com.example.account_service.dto.AccountResponse;
import com.example.account_service.dto.CreateAccountRequest;
import com.example.account_service.dto.UpdateAccountRequest;
import com.example.account_service.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import java.util.UUID;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(
        name = "Accounts",
        description = "Create and manage customer accounts"
)

public class AccountController {

    private final AccountService accountService;
    private static final String CORRELATION_ID_HEADER =
            "X-Correlation-ID";
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    @Operation(summary = "Create a new account")
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,

            @RequestHeader(
                    value = CORRELATION_ID_HEADER,
                    required = false
            )
            String correlationId,

            @Valid @RequestBody CreateAccountRequest request
    ) {
        String effectiveCorrelationId =
                correlationId == null || correlationId.isBlank()
                        ? UUID.randomUUID().toString()
                        : correlationId;

        AccountResponse createdAccount =
                accountService.createAccount(
                        request,
                        authorizationHeader,
                        effectiveCorrelationId
                );

        URI location = URI.create(
                "/api/accounts/" + createdAccount.id()
        );

        return ResponseEntity
                .created(location)
                .header(
                        CORRELATION_ID_HEADER,
                        effectiveCorrelationId
                )
                .body(createdAccount);
    }


    //GET
    @Operation(summary = "Get an account by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable Long id
    ) {
        AccountResponse account =
                accountService.getAccount(id);

        return ResponseEntity.ok(account);
    }

    // GET ALL ACCOUNTS
    @Operation(summary = "Get all accounts")
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> accounts =
                accountService.getAllAccounts();

        return ResponseEntity.ok(accounts);
    }
 // PUT request to update data
 @Operation(summary = "Update an existing account")
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountRequest request
    ) {
        AccountResponse updatedAccount =
                accountService.updateAccount(id, request);

        return ResponseEntity.ok(updatedAccount);
    }

    // delete
    @Operation(summary = "Delete an account")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long id
    ) {
        accountService.deleteAccount(id);

        return ResponseEntity.noContent().build();
    }


}
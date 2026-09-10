package com.example.account_service.controller;

import com.example.account_service.dto.AccountResponse;
import com.example.account_service.dto.CreateAccountRequest;
import com.example.account_service.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        AccountResponse createdAccount =
                accountService.createAccount(request);

        URI location = URI.create(
                "/api/accounts/" + createdAccount.id()
        );

        return ResponseEntity
                .created(location)
                .body(createdAccount);
    }


    //GET
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable Long id
    ) {
        AccountResponse account =
                accountService.getAccount(id);

        return ResponseEntity.ok(account);
    }

    // GET ALL ACCOUNTS
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> accounts =
                accountService.getAllAccounts();

        return ResponseEntity.ok(accounts);
    }

}
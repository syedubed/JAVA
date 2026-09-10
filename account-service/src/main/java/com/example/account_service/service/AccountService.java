package com.example.account_service.service;

import com.example.account_service.dto.AccountResponse;
import com.example.account_service.dto.CreateAccountRequest;
import com.example.account_service.exception.AccountNotFoundException;
import com.example.account_service.exception.DuplicateAccountNumberException;
import com.example.account_service.model.Account;
import com.example.account_service.model.AccountStatus;
import com.example.account_service.repository.InMemoryAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final InMemoryAccountRepository accountRepository;

    public AccountService(
            InMemoryAccountRepository accountRepository
    ) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse createAccount(
            CreateAccountRequest request
    ) {
        if (accountRepository.existsByAccountNumber(
                request.accountNumber()
        )) {
            throw new DuplicateAccountNumberException(
                    request.accountNumber()
            );
        }

        Account newAccount = new Account(
                null,
                request.profileId(),
                request.accountNumber(),
                request.accountType(),
                AccountStatus.ACTIVE
        );

        Account savedAccount =
                accountRepository.save(newAccount);

        return toResponse(savedAccount);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.id(),
                account.profileId(),
                account.accountNumber(),
                account.accountType(),
                account.status()
        );
    }

    public AccountResponse getAccount(Long id) {
        Account account = accountRepository
                .findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException(id)
                );

        return toResponse(account);
    }
}
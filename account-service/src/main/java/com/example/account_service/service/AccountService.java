package com.example.account_service.service;

import com.example.account_service.dto.AccountResponse;
import com.example.account_service.dto.CreateAccountRequest;
import com.example.account_service.dto.UpdateAccountRequest;
import com.example.account_service.exception.AccountNotFoundException;
import com.example.account_service.exception.DuplicateAccountNumberException;
import com.example.account_service.model.Account;
import com.example.account_service.model.AccountStatus;
import com.example.account_service.repository.InMemoryAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
 // get acc by ID
    public AccountResponse getAccount(Long id) {
        Account account = accountRepository
                .findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException(id)
                );

        return toResponse(account);
    }

    //to get all accounts

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // PUT REQUEST TO UPdate

    public AccountResponse updateAccount(
            Long id,
            UpdateAccountRequest request
    ) {
        Account currentAccount = accountRepository
                .findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException(id)
                );

        Account updatedAccount = new Account(
                currentAccount.id(),
                currentAccount.profileId(),
                currentAccount.accountNumber(),
                request.accountType(),
                request.status()
        );

        Account savedAccount =
                accountRepository.update(updatedAccount);

        return toResponse(savedAccount);
    }

    // delete
    public void deleteAccount(Long id) {
        boolean deleted = accountRepository.deleteById(id);

        if (!deleted) {
            throw new AccountNotFoundException(id);
        }
    }
}
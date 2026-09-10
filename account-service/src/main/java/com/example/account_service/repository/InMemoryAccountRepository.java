package com.example.account_service.repository;

import com.example.account_service.model.Account;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryAccountRepository {

    private final ConcurrentHashMap<Long, Account> accounts =
            new ConcurrentHashMap<>();

    private final AtomicLong nextId = new AtomicLong();

    public Account save(Account account) {
        long id = nextId.incrementAndGet();

        Account savedAccount = new Account(
                id,
                account.profileId(),
                account.accountNumber(),
                account.accountType(),
                account.status()
        );

        accounts.put(id, savedAccount);

        return savedAccount;
    }

    public boolean existsByAccountNumber(String accountNumber) {
        return accounts.values()
                .stream()
                .anyMatch(account ->
                        account.accountNumber()
                                .equalsIgnoreCase(accountNumber)
                );
    }
            // TO GET by ID
    public Optional<Account> findById(Long id) {
        return Optional.ofNullable(accounts.get(id));
    }

    // to get all the accounts

    public List<Account> findAll() {
        return accounts.values()
                .stream()
                .sorted(Comparator.comparing(Account::id))
                .toList();
    }
}
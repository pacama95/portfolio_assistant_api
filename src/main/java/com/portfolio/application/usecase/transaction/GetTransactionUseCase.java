package com.portfolio.application.usecase.transaction;

import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.domain.port.TransactionRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Use case for retrieving transactions
 */
@ApplicationScoped
public class GetTransactionUseCase {

    @Inject
    TransactionRepository transactionRepository;

    /**
     * Gets a transaction by ID
     */
    @WithSession
    public Uni<Transaction> getById(UUID id) {
        return transactionRepository.findById(id);
    }

    /**
     * Gets all transactions for a specific ticker
     */
    public Multi<Transaction> getByTicker(String ticker) {
        return transactionRepository.findByTicker(ticker)
            .onItem()
            .transformToMulti(list -> Multi.createFrom().iterable(list));
    }

    /**
     * Gets all transactions (active and inactive)
     */
    public Multi<Transaction> getAll() {
        return transactionRepository.findAll()
            .onItem()
            .transformToMulti(list -> Multi.createFrom().iterable(list));
    }

    /**
     * Searches transactions by criteria
     */
    public Multi<Transaction> searchTransactions(String ticker, TransactionType type, 
                                               LocalDate fromDate, LocalDate toDate) {
        return transactionRepository.searchTransactions(ticker, type, fromDate, toDate)
            .onItem()
            .transformToMulti(list -> Multi.createFrom().iterable(list));
    }

    /**
     * Checks if a transaction exists
     */
    @WithSession
    public Uni<Boolean> exists(UUID id) {
        return transactionRepository.existsById(id);
    }

    /**
     * Counts total transactions
     */
    @WithSession
    public Uni<Long> countAll() {
        return transactionRepository.countAll();
    }

    /**
     * Counts transactions for a ticker
     */
    @WithSession
    public Uni<Long> countByTicker(String ticker) {
        return transactionRepository.countByTicker(ticker);
    }
} 
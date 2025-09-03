package com.portfolio.infrastructure.persistence.adapter;

import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.domain.port.TransactionRepository;
import com.portfolio.infrastructure.persistence.entity.TransactionEntity;
import com.portfolio.infrastructure.persistence.repository.TransactionPanacheRepository;
import com.portfolio.infrastructure.persistence.mapper.TransactionEntityMapper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Adapter for TransactionRepository port implementation
 */
@ApplicationScoped
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionPanacheRepository panacheRepository;
    private final TransactionEntityMapper transactionEntityMapper;

    public TransactionRepositoryAdapter(TransactionPanacheRepository panacheRepository, TransactionEntityMapper transactionEntityMapper) {
        this.panacheRepository = panacheRepository;
        this.transactionEntityMapper = transactionEntityMapper;
    }

    @Override
    public Uni<Transaction> save(Transaction transaction) {
        return Uni.createFrom().item(() -> transactionEntityMapper.toEntity(transaction))
                .flatMap(panacheRepository::persistAndFlush)
                .map(transactionEntity ->
                        transactionEntityMapper.toDomain(transactionEntity, transaction.getDomainEvents()));
    }

    @Override
    public Uni<Transaction> findById(UUID id) {
        return panacheRepository.findById(id)
            .map(transactionEntityMapper::toDomain);
    }

    @Override
    public Uni<List<Transaction>> findByTicker(String ticker) {
        return panacheRepository.findByTicker(ticker)
            .map(entities -> entities.stream()
                .map(transactionEntityMapper::toDomain)
                .toList());
    }

    @Override
    public Uni<List<Transaction>> findAll() {
        return panacheRepository.findAllOrderedByDate()
            .map(entities -> entities.stream()
                .map(transactionEntityMapper::toDomain)
                .toList());
    }

    @Override
    public Uni<List<Transaction>> searchTransactions(String ticker,
                                                     TransactionType type,
                                                     LocalDate fromDate,
                                                     LocalDate toDate) {
        return panacheRepository.searchTransactions(ticker, type, fromDate, toDate)
            .map(entities -> entities.stream()
                .map(transactionEntityMapper::toDomain)
                .toList());
    }

    @Override
    public Uni<Transaction> update(Transaction transaction) {
        return Uni.createFrom().item(() -> transactionEntityMapper.toEntity(transaction))
                .flatMap(transactionEntity ->
                        panacheRepository.getSession().flatMap(session -> session.merge(transactionEntity)))
                .flatMap(panacheRepository::persistAndFlush)
                .map(transactionEntity ->
                        transactionEntityMapper.toDomain(transactionEntity, transaction.popEvents()));
    }

    @Override
    public Uni<Boolean> deleteById(UUID id) {
        return panacheRepository.deleteById(id);
    }

    @Override
    public Uni<Boolean> existsById(UUID id) {
        return panacheRepository.findByIdActive(id)
            .map(Objects::nonNull);
    }

    @Override
    public Uni<Long> countAll() {
        return panacheRepository.count();
    }

    @Override
    public Uni<Long> countByTicker(String ticker) {
        return panacheRepository.countByTicker(ticker);
    }
} 
package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.domain.model.TransactionType;
import com.portfolio.infrastructure.persistence.entity.TransactionEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Panache reactive repository for TransactionEntity
 */
@ApplicationScoped
public class TransactionPanacheRepository implements PanacheRepository<TransactionEntity> {

    // READ operations - use @WithSession
    @WithSession
    public Uni<TransactionEntity> findById(UUID id) {
        return find("id = ?1", id).firstResult();
    }

    @WithSession
    public Uni<List<TransactionEntity>> findByTicker(String ticker) {
        return find("ticker = ?1 ORDER BY transactionDate DESC", ticker).list();
    }

    @WithSession
    public Uni<List<TransactionEntity>> findAllOrderedByDate() {
        return find("ORDER BY transactionDate DESC").list();
    }

    @WithSession
    public Uni<List<TransactionEntity>> searchTransactions(String ticker, TransactionType type, 
                                                          LocalDate fromDate, LocalDate toDate) {
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (ticker != null && !ticker.trim().isEmpty()) {
            query.append(" AND ticker = :ticker");
            params.put("ticker", ticker);
        }

        if (type != null) {
            query.append(" AND transactionType = :type");
            params.put("type", type);
        }

        if (fromDate != null) {
            query.append(" AND transactionDate >= :fromDate");
            params.put("fromDate", fromDate);
        }

        if (toDate != null) {
            query.append(" AND transactionDate <= :toDate");
            params.put("toDate", toDate);
        }

        query.append(" ORDER BY transactionDate DESC");

        return find(query.toString(), params).list();
    }

    @WithSession
    public Uni<Boolean> existsByTicker(String ticker) {
        return find("ticker = ?1", ticker)
            .count()
            .map(count -> count > 0);
    }

    @WithSession
    public Uni<Long> countByTicker(String ticker) {
        return find("ticker = ?1", ticker).count();
    }

    @WithSession
    public Uni<TransactionEntity> findByIdActive(UUID id) {
        return find("id = ?1", id).firstResult();
    }

    // WRITE operations - use @WithTransaction
    @WithTransaction
    public Uni<Boolean> deleteByTicker(String ticker) {
        return delete("ticker = ?1", ticker)
            .map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Boolean> softDeleteById(UUID id) {
        return delete("id = ?1", id)
            .map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Boolean> deleteById(UUID id) {
        return softDeleteById(id);
    }
} 
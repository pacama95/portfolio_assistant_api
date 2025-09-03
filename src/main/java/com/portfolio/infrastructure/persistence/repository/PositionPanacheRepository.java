package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.PositionEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Panache reactive repository for PositionEntity
 */
@ApplicationScoped
public class PositionPanacheRepository implements PanacheRepository<PositionEntity> {

    @Inject
    TransactionPanacheRepository transactionRepository;

    @WithSession
    public Uni<PositionEntity> findById(UUID id) {
        return find("id = ?1", id).firstResult();
    }

    @WithSession
    public Uni<PositionEntity> findByTicker(String ticker) {
        return find("ticker = ?1", ticker).firstResult();
    }

    @WithSession
    public Uni<List<PositionEntity>> findAllWithShares() {
        return find("currentQuantity > 0 ORDER BY ticker").list();
    }

    @WithSession
    public Uni<List<PositionEntity>> findAllActive() {
        return find("ORDER BY ticker").list();
    }

    @WithSession
    public Uni<Boolean> existsByTicker(String ticker) {
        return find("ticker = ?1", ticker)
            .count()
            .map(count -> count > 0);
    }

    @WithSession
    public Uni<Long> countWithShares() {
        return find("currentQuantity > 0").count();
    }

    @WithTransaction
    public Uni<PositionEntity> updateMarketPrice(String ticker, BigDecimal newPrice) {
        return findByTicker(ticker)
            .flatMap(position -> {
                if (position == null) {
                    return Uni.createFrom().nullItem();
                }
                
                position.setCurrentPrice(newPrice);
                position.setLastPriceUpdate(OffsetDateTime.now());
                
                // Recalculate market value and unrealized gain/loss
                BigDecimal marketValue = position.getCurrentQuantity().multiply(newPrice);
                position.setCurrentMarketValue(marketValue);

                BigDecimal costBasis = position.getTotalCostBasis();
                position.setUnrealizedGainLoss(marketValue.subtract(costBasis));
                
                return persistAndFlush(position);
            });
    }

    @WithTransaction
    public Uni<PositionEntity> recalculatePosition(String ticker) {
        // Call the stored procedure to recalculate position from transactions using a native query
        return Panache.getSession()
            .flatMap(session -> session
                .createNativeQuery("select recalculate_position(?1)")
                .setParameter(1, ticker)
                .getSingleResult())
            .flatMap(ignored -> findByTicker(ticker));
    }
} 
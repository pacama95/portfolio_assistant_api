package com.portfolio.domain.port;

import com.portfolio.domain.model.Position;
import io.smallrye.mutiny.Uni;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Port interface for position persistence operations
 */
public interface PositionRepository {

    /**
     * Finds a position by its ID
     */
    Uni<Position> findById(UUID id);

    /**
     * Finds a position by ticker symbol
     */
    Uni<Position> findByTicker(String ticker);

    /**
     * Finds all active positions (with shares > 0)
     */
    Uni<List<Position>> findAllWithShares();

    /**
     * Finds all positions (including zero positions)
     */
    Uni<List<Position>> findAll();

    /**
     * Updates market price for a specific ticker
     */
    Uni<Position> updateMarketPrice(String ticker, BigDecimal newPrice);

    /**
     * Recalculates position based on current transactions
     */
    Uni<Position> recalculatePosition(String ticker);

    /**
     * Checks if a position exists for a ticker
     */
    Uni<Boolean> existsByTicker(String ticker);

    /**
     * Counts total number of positions
     */
    Uni<Long> countAll();

    /**
     * Counts positions with shares > 0
     */
    Uni<Long> countWithShares();
} 
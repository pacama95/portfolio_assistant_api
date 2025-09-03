package com.portfolio.application.usecase.position;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Position;
import com.portfolio.domain.model.CurrentPosition;
import com.portfolio.domain.port.PositionRepository;
import com.portfolio.domain.port.MarketDataService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Use case for retrieving positions with real-time market data
 */
@ApplicationScoped
@Slf4j
public class GetPositionUseCase {

    @Inject
    PositionRepository positionRepository;

    @Inject
    MarketDataService marketDataService;

    /**
     * Gets a position by ID with real-time current price
     */
    @WithSession
    public Uni<CurrentPosition> getById(UUID id) {
        return positionRepository.findById(id)
                .chain(this::enrichWithCurrentPrice)
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error getting position with ID %s".formatted(id),
                                throwable));
    }

    /**
     * Gets a position by ticker symbol with real-time current price
     */
    @WithSession
    public Uni<CurrentPosition> getByTicker(String ticker) {
        return positionRepository.findByTicker(ticker)
                .chain(this::enrichWithCurrentPrice)
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error getting position with ticker %s".formatted(ticker),
                                throwable));
    }

    /**
     * Gets all positions including zero positions with real-time current prices
     */
    public Uni<List<CurrentPosition>> getAll() {
        return positionRepository.findAll()
                .chain(this::enrichListWithCurrentPrices)
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error getting all positions",
                                throwable));
    }

    /**
     * Gets only active positions (with shares > 0) with real-time current prices
     */
    public Uni<List<CurrentPosition>> getActivePositions() {
        return positionRepository.findAllWithShares()
                .chain(this::enrichListWithCurrentPrices)
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error getting all active positions",
                                throwable));
    }

    /**
     * Checks if a position exists for a ticker
     */
    @WithSession
    public Uni<Boolean> existsByTicker(String ticker) {
        return positionRepository.existsByTicker(ticker)
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error checking if position with ticker %s exists".formatted(ticker),
                                throwable));
    }

    /**
     * Counts total positions
     */
    @WithSession
    public Uni<Long> countAll() {
        return positionRepository.countAll()
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                                "Error getting positions  count",
                                throwable));
    }

    /**
     * Counts active positions (with shares > 0)
     */
    @WithSession
    public Uni<Long> countActivePositions() {
        return positionRepository.countWithShares().onFailure().transform(throwable ->
                new ServiceException(Errors.GetPosition.PERSISTENCE_ERROR,
                        "Error getting active positions count",
                        throwable));
    }

    /**
     * Enriches a single position with real-time current price from market data service
     */
    private Uni<CurrentPosition> enrichWithCurrentPrice(Position position) {
        if (position == null || position.getTicker() == null) {
            log.warn("Position or ticker is null, returning position with zero current price");
            return Uni.createFrom().nullItem();
        }

        return marketDataService.getCurrentPrice(position.getTicker())
                .map(currentPrice -> {
                    log.info("Retrieved current price {} for ticker {}", currentPrice, position.getTicker());
                    return new CurrentPosition(position, currentPrice);
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Failed to fetch current price for ticker {}, using stored price", 
                             position.getTicker(), throwable);
                    // Fall back to stored current price if market data service fails
                    BigDecimal fallbackPrice = position.getCurrentPrice() != null ? 
                                             position.getCurrentPrice() : BigDecimal.ZERO;
                    // Use the position's lastUpdated date as the price timestamp since this is stored data
                    LocalDateTime fallbackTimestamp = position.getLastUpdated() != null ?
                                                    position.getLastUpdated().atStartOfDay() : 
                                                    LocalDateTime.now().minusDays(1);
                    return new CurrentPosition(position, fallbackPrice, fallbackTimestamp);
                });
    }

    /**
     * Enriches a list of positions with real-time current prices
     */
    private Uni<List<CurrentPosition>> enrichListWithCurrentPrices(List<Position> positions) {
        if (positions == null || positions.isEmpty()) {
            return Uni.createFrom().item(List.of());
        }

        return Multi.createFrom().iterable(positions)
                .onItem().transformToUniAndConcatenate(this::enrichWithCurrentPrice)
                .collect().asList();
    }
} 
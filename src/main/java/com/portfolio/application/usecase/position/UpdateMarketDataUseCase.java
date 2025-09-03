package com.portfolio.application.usecase.position;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Position;
import com.portfolio.domain.port.PositionRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;

/**
 * Use case for updating market data for positions
 */
@ApplicationScoped
public class UpdateMarketDataUseCase {

    @Inject
    PositionRepository positionRepository;

    /**
     * Updates the market price for a specific ticker
     */
    @WithTransaction
    public Uni<Position> execute(String ticker, BigDecimal newPrice) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return Uni.createFrom().failure(new ServiceException(Errors.UpdateMarketData.INVALID_INPUT, "Ticker cannot be null or empty"));
        }

        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return Uni.createFrom().failure(new ServiceException(Errors.UpdateMarketData.INVALID_INPUT, "Price must be positive"));
        }

        return positionRepository.existsByTicker(ticker)
            .flatMap(exists -> {
                if (!exists) {
                    return Uni.createFrom().failure(
                        new ServiceException(Errors.UpdateMarketData.NOT_FOUND, "No position found for ticker: " + ticker)
                    );
                }
                
                return positionRepository.updateMarketPrice(ticker, newPrice)
                        .onFailure().transform(throwable ->
                                new ServiceException(Errors.UpdateMarketData.PERSISTENCE_ERROR,
                                        "Error updating market price for ticker %s to price %s".formatted(ticker, newPrice),
                                        throwable));
            });
    }
} 
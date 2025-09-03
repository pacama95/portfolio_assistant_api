package com.portfolio.application.usecase.position;

import com.portfolio.domain.exception.Error;
import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Position;
import com.portfolio.domain.port.PositionRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Use case for recalculating positions
 */
@ApplicationScoped
public class RecalculatePositionUseCase {

    @Inject
    PositionRepository positionRepository;

    /**
     * Recalculates a position for a specific ticker
     */
    @WithTransaction
    public Uni<Position> execute(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return Uni.createFrom().failure(new ServiceException(Errors.RecalculatePosition.INVALID_INPUT, "Ticker cannot be null or empty"));
        }

        return positionRepository.recalculatePosition(ticker)
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.RecalculatePosition.PERSISTENCE_ERROR,
                                "Error recalculating position for ticker %s".formatted(ticker),
                                throwable));
    }
} 
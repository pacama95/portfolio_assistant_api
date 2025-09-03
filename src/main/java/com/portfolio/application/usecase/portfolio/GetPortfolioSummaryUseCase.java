package com.portfolio.application.usecase.portfolio;

import com.portfolio.application.usecase.position.GetPositionUseCase;
import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.CurrentPosition;
import com.portfolio.domain.model.PortfolioSummary;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Use case for calculating portfolio summary with real-time market data
 */
@ApplicationScoped
@Slf4j
public class GetPortfolioSummaryUseCase {

    @Inject
    GetPositionUseCase getPositionUseCase;

    /**
     * Gets summary for all positions with real-time current prices
     */
    @WithSession
    public Uni<PortfolioSummary> getPortfolioSummary() {
        log.info("Calculating portfolio summary with real-time market data");
        return getPositionUseCase.getAll()
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPortfolioSummary.PERSISTENCE_ERROR, "Error getting all positions", throwable))
            .map(this::calculateSummary);
    }

    /**
     * Gets summary for active positions only (shares > 0) with real-time current prices
     */
    @WithSession
    public Uni<PortfolioSummary> getActiveSummary() {
        log.info("Calculating active portfolio summary with real-time market data");
        return getPositionUseCase.getActivePositions()
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPortfolioSummary.PERSISTENCE_ERROR, "Error getting all positions with shares", throwable))
            .map(this::calculateSummary);
    }

    /**
     * Calculate portfolio summary from current positions with real-time market prices
     */
    private PortfolioSummary calculateSummary(List<CurrentPosition> positions) {
        if (positions.isEmpty()) {
            return PortfolioSummary.empty();
        }

        BigDecimal totalMarketValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        long activePositions = 0;

        for (CurrentPosition position : positions) {
            if (position.hasShares()) {
                activePositions++;
            }
            BigDecimal marketValue = position.getMarketValue() != null ? position.getMarketValue() : BigDecimal.ZERO;
            BigDecimal positionCost = position.getTotalCost() != null ? position.getTotalCost() : BigDecimal.ZERO;
            
            totalMarketValue = totalMarketValue.add(marketValue);
            totalCost = totalCost.add(positionCost);
            
            log.debug("Position {}: Market Value = {}, Cost = {}", 
                     position.getTicker(), marketValue, positionCost);
        }

        BigDecimal totalUnrealizedGainLoss = totalMarketValue.subtract(totalCost);
        BigDecimal totalUnrealizedGainLossPercentage = BigDecimal.ZERO;

        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            totalUnrealizedGainLossPercentage = totalUnrealizedGainLoss
                .divide(totalCost, 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(6, RoundingMode.HALF_UP);
        }

        return new PortfolioSummary(
            totalMarketValue,
            totalCost,
            totalUnrealizedGainLoss,
            totalUnrealizedGainLossPercentage,
            positions.size(),
            activePositions
        );
    }
} 
package com.portfolio.domain.model;

import java.math.BigDecimal;

/**
 * Immutable value object representing portfolio summary metrics
 */
public record PortfolioSummary(
    BigDecimal totalMarketValue,
    BigDecimal totalCost,
    BigDecimal totalUnrealizedGainLoss,
    BigDecimal totalUnrealizedGainLossPercentage,
    long totalPositions,
    long activePositions
) {
    /**
     * Creates an empty portfolio summary
     */
    public static PortfolioSummary empty() {
        return new PortfolioSummary(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0L, 
            0L
        );
    }

    /**
     * Checks if the portfolio has any value
     */
    public boolean hasValue() {
        return totalMarketValue.compareTo(BigDecimal.ZERO) > 0 ||
               totalCost.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if there are any active positions
     */
    public boolean hasActivePositions() {
        return activePositions > 0;
    }
} 
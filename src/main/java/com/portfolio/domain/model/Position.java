package com.portfolio.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.math.RoundingMode;

/**
 * Position domain entity representing an aggregated position for a ticker
 */
public class Position {
    private UUID id;
    private String ticker;
    private BigDecimal totalQuantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal totalCost;
    private Currency currency;
    private LocalDate lastUpdated;
    private Boolean isActive;

    // Default constructor
    public Position() {
        this.isActive = true;
    }

    // Constructor with required fields
    public Position(String ticker, Currency currency) {
        this();
        this.ticker = ticker;
        this.currency = currency;
        this.lastUpdated = LocalDate.now();
        this.totalQuantity = BigDecimal.ZERO;
        this.averagePrice = BigDecimal.ZERO;
        this.currentPrice = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
    }

    /**
     * Checks if the position has any shares
     */
    public boolean hasShares() {
        return totalQuantity != null && totalQuantity.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Calculates the current market value
     */
    public BigDecimal getMarketValue() {
        return totalQuantity.multiply(currentPrice);
    }

    /**
     * Calculates the unrealized gain/loss
     */
    public BigDecimal getUnrealizedGainLoss() {
        return getMarketValue().subtract(totalCost);
    }

    /**
     * Calculates the unrealized gain/loss percentage
     */
    public BigDecimal getUnrealizedGainLossPercentage() {
        if (totalCost == null || totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getUnrealizedGainLoss().divide(totalCost, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }

    public BigDecimal getAveragePrice() { return averagePrice; }
    public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public LocalDate getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
} 
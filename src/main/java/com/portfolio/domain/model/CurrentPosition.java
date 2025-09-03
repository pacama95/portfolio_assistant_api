package com.portfolio.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.RoundingMode;

/**
 * CurrentPosition domain entity representing a position with real-time market data
 */
public class CurrentPosition {
    private UUID id;
    private String ticker;
    private BigDecimal totalQuantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal totalCost;
    private Currency currency;
    private LocalDate lastUpdated;
    private Boolean isActive;
    private LocalDateTime currentPriceTimestamp;

    // Default constructor
    public CurrentPosition() {
        this.isActive = true;
        this.currentPriceTimestamp = LocalDateTime.now();
    }

    // Constructor from Position with updated current price
    public CurrentPosition(Position position, BigDecimal realTimeCurrentPrice) {
        this.id = position.getId();
        this.ticker = position.getTicker();
        this.totalQuantity = position.getTotalQuantity();
        this.averagePrice = position.getAveragePrice();
        this.currentPrice = realTimeCurrentPrice;
        this.totalCost = position.getTotalCost();
        this.currency = position.getCurrency();
        this.lastUpdated = position.getLastUpdated();
        this.isActive = position.getIsActive();
        this.currentPriceTimestamp = LocalDateTime.now();
    }

    // Constructor from Position with updated current price and custom timestamp
    public CurrentPosition(Position position, BigDecimal realTimeCurrentPrice, LocalDateTime priceTimestamp) {
        this.id = position.getId();
        this.ticker = position.getTicker();
        this.totalQuantity = position.getTotalQuantity();
        this.averagePrice = position.getAveragePrice();
        this.currentPrice = realTimeCurrentPrice;
        this.totalCost = position.getTotalCost();
        this.currency = position.getCurrency();
        this.lastUpdated = position.getLastUpdated();
        this.isActive = position.getIsActive();
        this.currentPriceTimestamp = priceTimestamp;
    }

    // Constructor with required fields
    public CurrentPosition(String ticker, Currency currency, BigDecimal currentPrice) {
        this();
        this.ticker = ticker;
        this.currency = currency;
        this.currentPrice = currentPrice;
        this.lastUpdated = LocalDate.now();
        this.totalQuantity = BigDecimal.ZERO;
        this.averagePrice = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
    }

    /**
     * Checks if the position has any shares
     */
    public boolean hasShares() {
        return totalQuantity != null && totalQuantity.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Calculates the current market value using real-time price
     */
    public BigDecimal getMarketValue() {
        if (totalQuantity == null || currentPrice == null) {
            return BigDecimal.ZERO;
        }
        return totalQuantity.multiply(currentPrice);
    }

    /**
     * Calculates the unrealized gain/loss using real-time price
     */
    public BigDecimal getUnrealizedGainLoss() {
        return getMarketValue().subtract(totalCost != null ? totalCost : BigDecimal.ZERO);
    }

    /**
     * Calculates the unrealized gain/loss percentage using real-time price
     */
    public BigDecimal getUnrealizedGainLossPercentage() {
        if (totalCost == null || totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getUnrealizedGainLoss().divide(totalCost, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    /**
     * Checks if the current price data is fresh (updated within last 30 minutes)
     */
    public boolean isCurrentPriceFresh() {
        if (currentPriceTimestamp == null) {
            return false;
        }
        return currentPriceTimestamp.isAfter(LocalDateTime.now().minusMinutes(30));
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
    public void setCurrentPrice(BigDecimal currentPrice) { 
        this.currentPrice = currentPrice;
        this.currentPriceTimestamp = LocalDateTime.now();
    }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public LocalDate getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCurrentPriceTimestamp() { return currentPriceTimestamp; }
    public void setCurrentPriceTimestamp(LocalDateTime currentPriceTimestamp) { 
        this.currentPriceTimestamp = currentPriceTimestamp; 
    }
}

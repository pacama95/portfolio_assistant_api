package com.portfolio.infrastructure.persistence.entity;

import com.portfolio.domain.model.Currency;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for the positions table
 */
@Entity
@Table(
    name = "positions",
    indexes = {
        @Index(name = "idx_positions_ticker", columnList = "ticker")
    }
)
public class PositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "ticker", nullable = false, unique = true, length = 20)
    private String ticker;

    @Column(name = "current_quantity", nullable = false, precision = 18, scale = 6)
    private BigDecimal currentQuantity;

    @Column(name = "avg_cost_per_share", nullable = false, precision = 18, scale = 4)
    private BigDecimal avgCostPerShare;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_currency", nullable = false, columnDefinition = "currency_type")
    private Currency primaryCurrency;

    @Column(name = "total_cost_basis", nullable = false, precision = 18, scale = 4)
    private BigDecimal totalCostBasis;

    @Column(name = "total_commissions", precision = 18, scale = 4)
    private BigDecimal totalCommissions;

    @Column(name = "first_purchase_date")
    private LocalDate firstPurchaseDate;

    @Column(name = "last_transaction_date")
    private LocalDate lastTransactionDate;

    @Column(name = "unrealized_gain_loss", precision = 18, scale = 4)
    private BigDecimal unrealizedGainLoss;

    @Column(name = "current_market_value", precision = 18, scale = 4)
    private BigDecimal currentMarketValue;

    @Column(name = "current_price", precision = 18, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "last_price_update")
    private OffsetDateTime lastPriceUpdate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Default constructor
    public PositionEntity() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(BigDecimal currentQuantity) { this.currentQuantity = currentQuantity; }

    public BigDecimal getAvgCostPerShare() { return avgCostPerShare; }
    public void setAvgCostPerShare(BigDecimal avgCostPerShare) { this.avgCostPerShare = avgCostPerShare; }

    public Currency getPrimaryCurrency() { return primaryCurrency; }
    public void setPrimaryCurrency(Currency primaryCurrency) { this.primaryCurrency = primaryCurrency; }

    public BigDecimal getTotalCostBasis() { return totalCostBasis; }
    public void setTotalCostBasis(BigDecimal totalCostBasis) { this.totalCostBasis = totalCostBasis; }

    public BigDecimal getTotalCommissions() { return totalCommissions; }
    public void setTotalCommissions(BigDecimal totalCommissions) { this.totalCommissions = totalCommissions; }

    public LocalDate getFirstPurchaseDate() { return firstPurchaseDate; }
    public void setFirstPurchaseDate(LocalDate firstPurchaseDate) { this.firstPurchaseDate = firstPurchaseDate; }

    public LocalDate getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDate lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }

    public BigDecimal getUnrealizedGainLoss() { return unrealizedGainLoss; }
    public void setUnrealizedGainLoss(BigDecimal unrealizedGainLoss) { this.unrealizedGainLoss = unrealizedGainLoss; }

    public BigDecimal getCurrentMarketValue() { return currentMarketValue; }
    public void setCurrentMarketValue(BigDecimal currentMarketValue) { this.currentMarketValue = currentMarketValue; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public OffsetDateTime getLastPriceUpdate() { return lastPriceUpdate; }
    public void setLastPriceUpdate(OffsetDateTime lastPriceUpdate) { this.lastPriceUpdate = lastPriceUpdate; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
} 
package com.portfolio.infrastructure.rest.dto;

import com.portfolio.domain.model.Currency;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Position response with current position details and market values")
public record PositionResponse(
    @Schema(description = "Unique position identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    @Schema(description = "Stock ticker symbol", example = "AAPL")
    String ticker,
    @Schema(description = "Total number of shares held", example = "250.00")
    BigDecimal totalQuantity,
    @Schema(description = "Average purchase price per share", example = "145.50")
    BigDecimal averagePrice,
    @Schema(description = "Current market price per share", example = "155.25")
    BigDecimal currentPrice,
    @Schema(description = "Total cost basis of the position", example = "36375.00")
    BigDecimal totalCost,
    @Schema(description = "Position currency")
    Currency currency,
    @Schema(description = "Date when position was last updated", example = "2023-10-15")
    LocalDate lastUpdated,
    @Schema(description = "Whether the position is active (has shares > 0)")
    Boolean isActive,
    @Schema(description = "Current market value of the position", example = "38812.50")
    BigDecimal marketValue,
    @Schema(description = "Unrealized gain or loss", example = "2437.50")
    BigDecimal unrealizedGainLoss,
    @Schema(description = "Unrealized gain or loss as percentage", example = "6.70")
    BigDecimal unrealizedGainLossPercentage
) {} 
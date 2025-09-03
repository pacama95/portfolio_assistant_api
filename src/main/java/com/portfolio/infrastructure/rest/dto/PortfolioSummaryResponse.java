package com.portfolio.infrastructure.rest.dto;

import java.math.BigDecimal;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Portfolio summary with aggregated financial data")
public record PortfolioSummaryResponse(
    @Schema(description = "Total current market value of all positions", example = "125000.00")
    BigDecimal totalMarketValue,
    @Schema(description = "Total cost basis of all positions", example = "110000.00")
    BigDecimal totalCost,
    @Schema(description = "Total unrealized gain or loss", example = "15000.00")
    BigDecimal totalUnrealizedGainLoss,
    @Schema(description = "Total unrealized gain or loss as percentage", example = "13.64")
    BigDecimal totalUnrealizedGainLossPercentage,
    @Schema(description = "Total number of positions in portfolio", example = "10")
    long totalPositions,
    @Schema(description = "Number of active positions (with shares > 0)", example = "8")
    long activePositions
) {} 
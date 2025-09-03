package com.portfolio.infrastructure.rest.mapper;

import com.portfolio.domain.model.PortfolioSummary;
import com.portfolio.infrastructure.rest.dto.PortfolioSummaryResponse;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "cdi")
public interface PortfolioSummaryMapper {
    int MONETARY_SCALE = 4;
    RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Mapping(target = "totalMarketValue", expression = "java(normalizeMonetary(summary.totalMarketValue()))")
    @Mapping(target = "totalCost", expression = "java(normalizeMonetary(summary.totalCost()))")
    @Mapping(target = "totalUnrealizedGainLoss", expression = "java(normalizeMonetary(summary.totalUnrealizedGainLoss()))")
    @Mapping(target = "totalUnrealizedGainLossPercentage", expression = "java(normalizeMonetary(summary.totalUnrealizedGainLossPercentage()))")
    PortfolioSummaryResponse toResponse(PortfolioSummary summary);

    // Normalization helper
    default BigDecimal normalizeMonetary(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(MONETARY_SCALE, ROUNDING);
    }
} 
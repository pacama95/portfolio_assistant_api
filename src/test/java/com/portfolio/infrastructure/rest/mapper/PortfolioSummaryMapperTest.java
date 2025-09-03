package com.portfolio.infrastructure.rest.mapper;

import com.portfolio.domain.model.PortfolioSummary;
import com.portfolio.infrastructure.rest.dto.PortfolioSummaryResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioSummaryMapperTest {
    private final PortfolioSummaryMapper mapper = Mappers.getMapper(PortfolioSummaryMapper.class);

    @Test
    void testToResponse_normalizesFields() {
        PortfolioSummary summary = new PortfolioSummary(
            new BigDecimal("12345.67891"),
            new BigDecimal("12000.12345"),
            new BigDecimal("345.555555"),
            new BigDecimal("2.879999"),
            10L,
            8L
        );

        PortfolioSummaryResponse resp = mapper.toResponse(summary);

        assertEquals(new BigDecimal("12345.6789"), resp.totalMarketValue()); // scale 4
        assertEquals(new BigDecimal("12000.1235"), resp.totalCost()); // scale 4
        assertEquals(new BigDecimal("345.5556"), resp.totalUnrealizedGainLoss()); // scale 4
        assertEquals(new BigDecimal("2.8800"), resp.totalUnrealizedGainLossPercentage()); // scale 4
        assertEquals(10L, resp.totalPositions());
        assertEquals(8L, resp.activePositions());
    }
} 
package com.portfolio.infrastructure.persistence.mapper;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.Position;
import com.portfolio.infrastructure.persistence.entity.PositionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PositionEntityMapperTest {
    private PositionEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PositionEntityMapper.class);
    }

    @Test
    void testToDomain() {
        PositionEntity entity = new PositionEntity();
        var now = LocalDate.now();
        entity.setTicker("AAPL");
        entity.setCurrentQuantity(BigDecimal.valueOf(100));
        entity.setAvgCostPerShare(BigDecimal.valueOf(150));
        entity.setTotalCostBasis(BigDecimal.valueOf(20));
        entity.setPrimaryCurrency(Currency.USD);
        entity.setLastTransactionDate(now);
        entity.setUnrealizedGainLoss(BigDecimal.valueOf(1000));
        entity.setCurrentPrice(BigDecimal.valueOf(10));

        Position position = mapper.toDomain(entity);

        assertEquals("AAPL", position.getTicker());
        assertEquals(BigDecimal.valueOf(100), position.getTotalQuantity());
        assertEquals(BigDecimal.valueOf(150), position.getAveragePrice());
        assertEquals(BigDecimal.valueOf(20), position.getTotalCost());
        assertEquals(BigDecimal.valueOf(1000), position.getMarketValue());
        assertEquals(Currency.USD, position.getCurrency());
        assertEquals(now, position.getLastUpdated());
        assertEquals(BigDecimal.valueOf(980), position.getUnrealizedGainLoss());
    }
} 
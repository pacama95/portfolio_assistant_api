package com.portfolio.infrastructure.rest.mapper;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.CurrentPosition;
import com.portfolio.domain.model.Position;
import com.portfolio.infrastructure.rest.dto.PositionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PositionMapperTest {
    private final PositionMapper mapper = Mappers.getMapper(PositionMapper.class);

    @Test
    void testToResponse_normalizesFields() {
        Position pos = new Position();
        pos.setId(UUID.randomUUID());
        pos.setTicker("GOOG");
        pos.setTotalQuantity(new BigDecimal("10.1234567"));
        pos.setAveragePrice(new BigDecimal("100.987654"));
        pos.setCurrentPrice(new BigDecimal("101.234567"));
        pos.setTotalCost(new BigDecimal("1010.987654"));
        pos.setCurrency(Currency.USD);
        pos.setLastUpdated(LocalDate.of(2024, 3, 3));
        pos.setIsActive(true);

        PositionResponse resp = mapper.toResponse(pos);

        assertEquals(new BigDecimal("10.123457"), resp.totalQuantity()); // scale 6
        assertEquals(new BigDecimal("100.9877"), resp.averagePrice()); // scale 4
        assertEquals(new BigDecimal("101.2346"), resp.currentPrice()); // scale 4
        assertEquals(new BigDecimal("1010.9877"), resp.totalCost()); // scale 4
    }

    @Test
    void testCurrentPositionToResponse_withRealTimePrice() {
        // Given
        CurrentPosition currentPosition = createTestCurrentPosition("AAPL", new BigDecimal("175.50"));
        
        // When
        PositionResponse response = mapper.toResponse(currentPosition);
        
        // Then
        assertNotNull(response);
        assertEquals(currentPosition.getId(), response.id());
        assertEquals("AAPL", response.ticker());
        assertEquals(new BigDecimal("100.000000"), response.totalQuantity()); // Normalized to scale 6
        assertEquals(new BigDecimal("150.0000"), response.averagePrice()); // Normalized to scale 4
        assertEquals(new BigDecimal("175.5000"), response.currentPrice()); // Real-time price normalized to scale 4
        assertEquals(new BigDecimal("15000.0000"), response.totalCost()); // Normalized to scale 4
        assertEquals(Currency.USD, response.currency());
        assertTrue(response.isActive());
        
        // Market value should be calculated with real-time price: 100 * 175.50 = 17550
        assertEquals(new BigDecimal("17550.0000"), response.marketValue());
        // Unrealized gain/loss: 17550 - 15000 = 2550
        assertEquals(new BigDecimal("2550.0000"), response.unrealizedGainLoss());
        // Percentage: (2550/15000) * 100 = 17%
        assertEquals(new BigDecimal("17.0000"), response.unrealizedGainLossPercentage());
    }

    @Test
    void testCurrentPositionToResponse_withFallbackPrice() {
        // Given
        Position originalPosition = createTestPosition("MSFT");
        originalPosition.setCurrentPrice(new BigDecimal("300.25"));
        
        // Create CurrentPosition with fallback timestamp (stale data)
        CurrentPosition currentPosition = new CurrentPosition(
            originalPosition, 
            new BigDecimal("300.25"), 
            LocalDateTime.now().minusHours(2)
        );
        
        // When
        PositionResponse response = mapper.toResponse(currentPosition);
        
        // Then
        assertNotNull(response);
        assertEquals("MSFT", response.ticker());
        assertEquals(new BigDecimal("300.2500"), response.currentPrice()); // Fallback price
        // Market value with fallback price: 100 * 300.25 = 30025
        assertEquals(new BigDecimal("30025.0000"), response.marketValue());
    }

    @Test
    void testCurrentPositionToResponseList_withMultiplePositions() {
        // Given
        CurrentPosition position1 = createTestCurrentPosition("AAPL", new BigDecimal("175.50"));
        CurrentPosition position2 = createTestCurrentPosition("GOOGL", new BigDecimal("2650.75"));
        position2.setTotalQuantity(new BigDecimal("10"));
        position2.setTotalCost(new BigDecimal("25000.00"));
        
        List<CurrentPosition> positions = List.of(position1, position2);
        
        // When
        List<PositionResponse> responses = mapper.toCurrentPositionResponses(positions);
        
        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        
        // First position
        PositionResponse response1 = responses.get(0);
        assertEquals("AAPL", response1.ticker());
        assertEquals(new BigDecimal("175.5000"), response1.currentPrice());
        assertEquals(new BigDecimal("17550.0000"), response1.marketValue());
        
        // Second position
        PositionResponse response2 = responses.get(1);
        assertEquals("GOOGL", response2.ticker());
        assertEquals(new BigDecimal("2650.7500"), response2.currentPrice());
        // 10 * 2650.75 = 26507.5
        assertEquals(new BigDecimal("26507.5000"), response2.marketValue());
    }

    @ParameterizedTest
    @MethodSource("currentPositionFieldNormalizationData")
    void testCurrentPositionFieldNormalization(BigDecimal inputQuantity, BigDecimal inputPrice, 
                                              BigDecimal expectedQuantity, BigDecimal expectedPrice) {
        // Given
        CurrentPosition position = createTestCurrentPosition("TEST", inputPrice);
        position.setTotalQuantity(inputQuantity);
        
        // When
        PositionResponse response = mapper.toResponse(position);
        
        // Then
        assertEquals(expectedQuantity, response.totalQuantity());
        assertEquals(expectedPrice, response.currentPrice());
    }

    static Stream<Arguments> currentPositionFieldNormalizationData() {
        return Stream.of(
            // quantity with excessive precision, price with excessive precision
            Arguments.of(
                new BigDecimal("123.1234567890"), 
                new BigDecimal("456.7890123456"),
                new BigDecimal("123.123457"), // normalized to scale 6
                new BigDecimal("456.7890")    // normalized to scale 4
            ),
            // zero values
            Arguments.of(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("0.000000"),
                new BigDecimal("0.0000")
            ),
            // large numbers
            Arguments.of(
                new BigDecimal("999999.999999"),
                new BigDecimal("999999.9999"),
                new BigDecimal("999999.999999"),
                new BigDecimal("999999.9999")
            )
        );
    }

    @Test
    void testCurrentPositionToResponse_withNullValues() {
        // Given
        CurrentPosition position = new CurrentPosition();
        position.setId(UUID.randomUUID());
        position.setTicker("NULL_TEST");
        position.setCurrency(Currency.USD);
        position.setIsActive(true);
        // Leave all numeric fields as null
        
        // When
        PositionResponse response = mapper.toResponse(position);
        
        // Then
        assertNotNull(response);
        assertEquals("NULL_TEST", response.ticker());
        assertNull(response.totalQuantity());
        assertNull(response.averagePrice());
        assertNull(response.currentPrice());
        assertNull(response.totalCost());
        // Market value should be zero when quantity or price is null
        assertEquals(new BigDecimal("0.0000"), response.marketValue());
        assertEquals(new BigDecimal("0.0000"), response.unrealizedGainLoss());
        assertEquals(new BigDecimal("0.0000"), response.unrealizedGainLossPercentage());
    }

    @Test
    void testCurrentPositionToResponse_calculatedFields() {
        // Given
        CurrentPosition position = createTestCurrentPosition("CALC_TEST", new BigDecimal("200.00"));
        position.setTotalQuantity(new BigDecimal("50"));
        position.setTotalCost(new BigDecimal("7500.00")); // 50 * 150 average price
        
        // When
        PositionResponse response = mapper.toResponse(position);
        
        // Then
        // Market value: 50 * 200 = 10000
        assertEquals(new BigDecimal("10000.0000"), response.marketValue());
        // Unrealized gain: 10000 - 7500 = 2500
        assertEquals(new BigDecimal("2500.0000"), response.unrealizedGainLoss());
        // Percentage: (2500 / 7500) * 100 = 33.33%
        assertEquals(new BigDecimal("33.3333"), response.unrealizedGainLossPercentage());
    }

    @Test
    void testCurrentPositionToResponse_negativeUnrealizedGainLoss() {
        // Given
        CurrentPosition position = createTestCurrentPosition("LOSS_TEST", new BigDecimal("120.00"));
        position.setTotalQuantity(new BigDecimal("100"));
        position.setTotalCost(new BigDecimal("15000.00")); // Higher than current market value
        
        // When
        PositionResponse response = mapper.toResponse(position);
        
        // Then
        // Market value: 100 * 120 = 12000
        assertEquals(new BigDecimal("12000.0000"), response.marketValue());
        // Unrealized loss: 12000 - 15000 = -3000
        assertEquals(new BigDecimal("-3000.0000"), response.unrealizedGainLoss());
        // Percentage: (-3000 / 15000) * 100 = -20%
        assertEquals(new BigDecimal("-20.0000"), response.unrealizedGainLossPercentage());
    }

    @Test
    void testCurrentPositionToResponse_preservesOriginalPositionFields() {
        // Given
        Position originalPosition = createTestPosition("PRESERVE_TEST");
        UUID originalId = UUID.randomUUID();
        LocalDate lastUpdated = LocalDate.of(2024, 1, 15);
        
        originalPosition.setId(originalId);
        originalPosition.setLastUpdated(lastUpdated);
        originalPosition.setIsActive(false);
        originalPosition.setCurrency(Currency.EUR);
        
        CurrentPosition currentPosition = new CurrentPosition(originalPosition, new BigDecimal("250.75"));
        
        // When
        PositionResponse response = mapper.toResponse(currentPosition);
        
        // Then
        assertEquals(originalId, response.id());
        assertEquals(lastUpdated, response.lastUpdated());
        assertFalse(response.isActive());
        assertEquals(Currency.EUR, response.currency());
        assertEquals(new BigDecimal("250.7500"), response.currentPrice()); // Real-time price
    }

    @Test
    void testCurrentPositionToResponse_emptyList() {
        // Given
        List<CurrentPosition> emptyList = List.of();
        
        // When
        List<PositionResponse> responses = mapper.toCurrentPositionResponses(emptyList);
        
        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    private CurrentPosition createTestCurrentPosition(String ticker, BigDecimal realTimePrice) {
        Position originalPosition = createTestPosition(ticker);
        return new CurrentPosition(originalPosition, realTimePrice);
    }

    private Position createTestPosition(String ticker) {
        Position position = new Position();
        position.setId(UUID.randomUUID());
        position.setTicker(ticker);
        position.setTotalQuantity(new BigDecimal("100"));
        position.setAveragePrice(new BigDecimal("150.00"));
        position.setCurrentPrice(new BigDecimal("160.00"));
        position.setTotalCost(new BigDecimal("15000.00"));
        position.setCurrency(Currency.USD);
        position.setLastUpdated(LocalDate.now().minusDays(1));
        position.setIsActive(true);
        return position;
    }
} 
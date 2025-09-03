package com.portfolio.application.usecase.portfolio;

import com.portfolio.application.usecase.position.GetPositionUseCase;
import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.CurrentPosition;
import com.portfolio.domain.model.Position;
import com.portfolio.domain.model.PortfolioSummary;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetPortfolioSummaryUseCaseTest {
    private GetPositionUseCase getPositionUseCase;
    private GetPortfolioSummaryUseCase portfolioSummaryUseCase;

    @BeforeEach
    void setUp() {
        getPositionUseCase = mock(GetPositionUseCase.class);
        portfolioSummaryUseCase = new GetPortfolioSummaryUseCase();
        portfolioSummaryUseCase.getPositionUseCase = getPositionUseCase;
    }

    @Test
    void testGetPortfolioSummaryWithEmptyPositions() {
        // Given
        when(getPositionUseCase.getAll()).thenReturn(Uni.createFrom().item(Collections.emptyList()));

        // When
        Uni<PortfolioSummary> uni = portfolioSummaryUseCase.getPortfolioSummary();
        PortfolioSummary summary = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(summary);
        assertEquals(BigDecimal.ZERO, summary.totalMarketValue());
        assertEquals(BigDecimal.ZERO, summary.totalCost());
        assertEquals(BigDecimal.ZERO, summary.totalUnrealizedGainLoss());
        assertEquals(BigDecimal.ZERO, summary.totalUnrealizedGainLossPercentage());
        assertEquals(0, summary.totalPositions());
        assertEquals(0, summary.activePositions());
        verify(getPositionUseCase).getAll();
    }

    @Test
    void testGetPortfolioSummaryWithCurrentPositions() {
        // Given
        CurrentPosition position1 = createCurrentPosition("AAPL", new BigDecimal("175.50"), true);
        CurrentPosition position2 = createCurrentPosition("MSFT", new BigDecimal("300.25"), false);
        
        List<CurrentPosition> positions = List.of(position1, position2);
        when(getPositionUseCase.getAll()).thenReturn(Uni.createFrom().item(positions));

        // When
        Uni<PortfolioSummary> uni = portfolioSummaryUseCase.getPortfolioSummary();
        PortfolioSummary summary = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(summary);
        // AAPL: 100 * 175.50 = 17550, MSFT: 0 * 300.25 = 0 → Total: 17550
        assertEquals(new BigDecimal("17550.00"), summary.totalMarketValue());
        // AAPL: 15000, MSFT: 15000 → Total: 30000
        assertEquals(new BigDecimal("30000.00"), summary.totalCost());
        // Gain: 17550 - 30000 = 2550
        assertEquals(new BigDecimal("-12450.00"), summary.totalUnrealizedGainLoss());
        // Percentage: (2550/15000) * 100 = 17%
        assertEquals(new BigDecimal("-41.500000"), summary.totalUnrealizedGainLossPercentage());
        assertEquals(2, summary.totalPositions());
        assertEquals(1, summary.activePositions());
        verify(getPositionUseCase).getAll();
    }

    @Test
    void testGetPortfolioSummaryWithRealTimeMarketData() {
        // Given
        CurrentPosition position1 = createCurrentPosition("AAPL", new BigDecimal("180.00"), true);
        position1.setCurrentPriceTimestamp(LocalDateTime.now()); // Fresh real-time data
        
        CurrentPosition position2 = createCurrentPosition("GOOGL", new BigDecimal("2600.00"), true);
        position2.setTotalQuantity(new BigDecimal("10"));
        position2.setTotalCost(new BigDecimal("25000.00"));
        position2.setCurrentPriceTimestamp(LocalDateTime.now()); // Fresh real-time data
        
        List<CurrentPosition> positions = List.of(position1, position2);
        when(getPositionUseCase.getAll()).thenReturn(Uni.createFrom().item(positions));

        // When
        Uni<PortfolioSummary> uni = portfolioSummaryUseCase.getPortfolioSummary();
        PortfolioSummary summary = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(summary);
        // AAPL: 100 * 180 = 18000, GOOGL: 10 * 2600 = 26000 → Total: 44000
        assertEquals(new BigDecimal("44000.00"), summary.totalMarketValue());
        // AAPL: 15000, GOOGL: 25000 → Total: 40000
        assertEquals(new BigDecimal("40000.00"), summary.totalCost());
        // Gain: 44000 - 40000 = 4000
        assertEquals(new BigDecimal("4000.00"), summary.totalUnrealizedGainLoss());
        // Percentage: (4000/40000) * 100 = 10%
        assertEquals(new BigDecimal("10.000000"), summary.totalUnrealizedGainLossPercentage());
        assertEquals(2, summary.totalPositions());
        assertEquals(2, summary.activePositions());
    }

    @Test
    void testGetActiveSummaryWithCurrentPositions() {
        // Given
        CurrentPosition activePosition = createCurrentPosition("TSLA", new BigDecimal("800.50"), true);
        List<CurrentPosition> activePositions = Collections.singletonList(activePosition);
        when(getPositionUseCase.getActivePositions()).thenReturn(Uni.createFrom().item(activePositions));

        // When
        Uni<PortfolioSummary> uni = portfolioSummaryUseCase.getActiveSummary();
        PortfolioSummary summary = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(summary);
        // TSLA: 100 * 800.50 = 80050
        assertEquals(new BigDecimal("80050.00"), summary.totalMarketValue());
        assertEquals(new BigDecimal("15000.00"), summary.totalCost());
        // Gain: 80050 - 15000 = 65050
        assertEquals(new BigDecimal("65050.00"), summary.totalUnrealizedGainLoss());
        // Percentage: (65050/15000) * 100 = 433.67%
        assertEquals(new BigDecimal("433.666667"), summary.totalUnrealizedGainLossPercentage());
        assertEquals(1, summary.totalPositions());
        assertEquals(1, summary.activePositions());
        verify(getPositionUseCase).getActivePositions();
    }

    @Test
    void testGetActiveSummaryWithMixedFreshAndStaleData() {
        // Given
        CurrentPosition freshPosition = createCurrentPosition("AAPL", new BigDecimal("175.50"), true);
        freshPosition.setCurrentPriceTimestamp(LocalDateTime.now()); // Fresh data
        
        CurrentPosition stalePosition = createCurrentPosition("MSFT", new BigDecimal("295.00"), true);
        stalePosition.setCurrentPriceTimestamp(LocalDateTime.now().minusHours(2)); // Stale fallback data
        
        List<CurrentPosition> positions = List.of(freshPosition, stalePosition);
        when(getPositionUseCase.getActivePositions()).thenReturn(Uni.createFrom().item(positions));

        // When
        Uni<PortfolioSummary> uni = portfolioSummaryUseCase.getActiveSummary();
        PortfolioSummary summary = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(summary);
        // Portfolio calculation uses current prices regardless of freshness
        // AAPL: 100 * 175.50 = 17550, MSFT: 100 * 295.00 = 29500 → Total: 47050
        assertEquals(new BigDecimal("47050.00"), summary.totalMarketValue());
        assertEquals(new BigDecimal("30000.00"), summary.totalCost()); // 15000 + 15000
        assertEquals(new BigDecimal("17050.00"), summary.totalUnrealizedGainLoss());
        assertEquals(2, summary.totalPositions());
        assertEquals(2, summary.activePositions());
    }

    @Test
    void testGetPortfolioSummaryHandlesNullValues() {
        // Given
        CurrentPosition positionWithNullCost = createCurrentPosition("NVDA", new BigDecimal("450.00"), true);
        positionWithNullCost.setTotalCost(null);
        
        CurrentPosition positionWithNullMarketValue = createCurrentPosition("AMD", new BigDecimal("100.00"), false);
        positionWithNullMarketValue.setTotalQuantity(null);
        
        List<CurrentPosition> positions = List.of(positionWithNullCost, positionWithNullMarketValue);
        when(getPositionUseCase.getAll()).thenReturn(Uni.createFrom().item(positions));

        // When
        Uni<PortfolioSummary> uni = portfolioSummaryUseCase.getPortfolioSummary();
        PortfolioSummary summary = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(summary);
        // NVDA: 100 * 450 = 45000, AMD: null quantity results in 0 market value → Total: 45000
        assertEquals(new BigDecimal("45000.00"), summary.totalMarketValue());
        // NVDA: null cost = 0, AMD: 15000 → Total: 15000
        assertEquals(new BigDecimal("15000.00"), summary.totalCost());
        assertEquals(new BigDecimal("30000.00"), summary.totalUnrealizedGainLoss());
        assertEquals(2, summary.totalPositions());
        assertEquals(1, summary.activePositions()); // Only NVDA has shares
    }

    @Test
    void testGetPortfolioSummaryWhenGetAllPositionsFails() {
        // Given
        RuntimeException positionException = new RuntimeException("Position service error");
        when(getPositionUseCase.getAll()).thenReturn(Uni.createFrom().failure(positionException));

        // When
        Uni<PortfolioSummary> uni = portfolioSummaryUseCase.getPortfolioSummary();
        UniAssertSubscriber<PortfolioSummary> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPortfolioSummary.PERSISTENCE_ERROR, thrown.getError());
        assertEquals("Error getting all positions", thrown.getMessage());
        assertEquals(positionException, thrown.getCause());
        verify(getPositionUseCase).getAll();
    }

    @Test
    void testGetActiveSummaryWhenGetActivePositionsFails() {
        // Given
        RuntimeException activePositionException = new RuntimeException("Active position service error");
        when(getPositionUseCase.getActivePositions()).thenReturn(Uni.createFrom().failure(activePositionException));

        // When
        Uni<PortfolioSummary> uni = portfolioSummaryUseCase.getActiveSummary();
        UniAssertSubscriber<PortfolioSummary> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPortfolioSummary.PERSISTENCE_ERROR, thrown.getError());
        assertEquals("Error getting all positions with shares", thrown.getMessage());
        assertEquals(activePositionException, thrown.getCause());
        verify(getPositionUseCase).getActivePositions();
    }

    private CurrentPosition createCurrentPosition(String ticker, BigDecimal currentPrice, boolean hasShares) {
        Position originalPosition = new Position();
        originalPosition.setId(UUID.randomUUID());
        originalPosition.setTicker(ticker);
        originalPosition.setTotalQuantity(hasShares ? new BigDecimal("100") : BigDecimal.ZERO);
        originalPosition.setAveragePrice(new BigDecimal("150.00"));
        originalPosition.setCurrentPrice(new BigDecimal("160.00")); // Stored price (fallback)
        originalPosition.setTotalCost(new BigDecimal("15000.00"));
        originalPosition.setCurrency(Currency.USD);
        originalPosition.setLastUpdated(LocalDate.now().minusDays(1));
        originalPosition.setIsActive(true);

        // Create CurrentPosition with real-time price
        CurrentPosition currentPosition = new CurrentPosition(originalPosition, currentPrice);
        return currentPosition;
    }
}
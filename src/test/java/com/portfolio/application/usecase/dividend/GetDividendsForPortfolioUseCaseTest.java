package com.portfolio.application.usecase.dividend;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.Dividend;
import com.portfolio.domain.model.Position;
import com.portfolio.domain.port.MarketDataService;
import com.portfolio.domain.port.PositionRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetDividendsForPortfolioUseCaseTest {
    private PositionRepository positionRepository;
    private MarketDataService marketDataService;
    private GetDividendsForPortfolioUseCase getDividendsUseCase;

    @BeforeEach
    void setUp() {
        positionRepository = mock(PositionRepository.class);
        marketDataService = mock(MarketDataService.class);
        getDividendsUseCase = new GetDividendsForPortfolioUseCase();
        getDividendsUseCase.positionRepository = positionRepository;
        getDividendsUseCase.marketDataService = marketDataService;
    }

    @Test
    void testExecuteSuccess() {
        // Given
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        Position position1 = createPosition("AAPL", new BigDecimal("100"));
        Position position2 = createPosition("MSFT", new BigDecimal("50"));
        List<Position> positions = Arrays.asList(position1, position2);
        
        Dividend aaplDividend = new Dividend("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 5, 12), new BigDecimal("0.24"));
        Dividend msftDividend = new Dividend("MSFT", "XNAS", "NASDAQ", LocalDate.of(2023, 6, 14), new BigDecimal("0.75"));
        
        when(positionRepository.findAllWithShares()).thenReturn(Uni.createFrom().item(positions));
        when(marketDataService.getDividends("AAPL", startDate, endDate))
            .thenReturn(Uni.createFrom().item(List.of(aaplDividend)));
        when(marketDataService.getDividends("MSFT", startDate, endDate))
            .thenReturn(Uni.createFrom().item(List.of(msftDividend)));

        // When
        Uni<Map<String, List<Dividend>>> uni = getDividendsUseCase.execute(startDate, endDate);
        Map<String, List<Dividend>> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("AAPL"));
        assertTrue(result.containsKey("MSFT"));
        assertEquals(1, result.get("AAPL").size());
        assertEquals(1, result.get("MSFT").size());
        assertEquals(new BigDecimal("0.24"), result.get("AAPL").get(0).getAmount());
        assertEquals(new BigDecimal("0.75"), result.get("MSFT").get(0).getAmount());
        
        verify(positionRepository).findAllWithShares();
        verify(marketDataService).getDividends("AAPL", startDate, endDate);
        verify(marketDataService).getDividends("MSFT", startDate, endDate);
    }

    @Test
    void testExecuteWithEmptyPositions() {
        // Given
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        when(positionRepository.findAllWithShares()).thenReturn(Uni.createFrom().item(Collections.emptyList()));

        // When
        Uni<Map<String, List<Dividend>>> uni = getDividendsUseCase.execute(startDate, endDate);
        Map<String, List<Dividend>> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(positionRepository).findAllWithShares();
        verifyNoInteractions(marketDataService);
    }





    @Test
    void testExecuteWithPositionRepositoryFailure() {
        // Given
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        RuntimeException repositoryException = new RuntimeException("Database error");
        
        when(positionRepository.findAllWithShares()).thenReturn(Uni.createFrom().failure(repositoryException));

        // When
        Uni<Map<String, List<Dividend>>> uni = getDividendsUseCase.execute(startDate, endDate);
        UniAssertSubscriber<Map<String, List<Dividend>>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetDividends.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("Failed to retrieve positions"));
        assertEquals(repositoryException, thrown.getCause());
        
        verify(positionRepository).findAllWithShares();
        verifyNoInteractions(marketDataService);
    }

    @Test
    void testExecuteWithPartialMarketDataFailure() {
        // Given
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        Position position1 = createPosition("AAPL", new BigDecimal("100"));
        Position position2 = createPosition("FAIL", new BigDecimal("50"));
        List<Position> positions = Arrays.asList(position1, position2);
        
        Dividend aaplDividend = new Dividend("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 5, 12), new BigDecimal("0.24"));
        
        when(positionRepository.findAllWithShares()).thenReturn(Uni.createFrom().item(positions));
        when(marketDataService.getDividends("AAPL", startDate, endDate))
            .thenReturn(Uni.createFrom().item(List.of(aaplDividend)));
        when(marketDataService.getDividends("FAIL", startDate, endDate))
            .thenReturn(Uni.createFrom().failure(new RuntimeException("Market data error")));

        // When
        Uni<Map<String, List<Dividend>>> uni = getDividendsUseCase.execute(startDate, endDate);
        Map<String, List<Dividend>> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then - Should not fail, but return empty list for failed ticker
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("AAPL"));
        assertTrue(result.containsKey("FAIL"));
        assertEquals(1, result.get("AAPL").size());
        assertEquals(0, result.get("FAIL").size()); // Empty list for failed ticker
        
        verify(positionRepository).findAllWithShares();
        verify(marketDataService).getDividends("AAPL", startDate, endDate);
        verify(marketDataService).getDividends("FAIL", startDate, endDate);
    }



    private Position createPosition(String ticker, BigDecimal quantity) {
        Position position = new Position(ticker, Currency.USD);
        position.setId(UUID.randomUUID());
        position.setTotalQuantity(quantity);
        position.setAveragePrice(new BigDecimal("100"));
        position.setTotalCost(quantity.multiply(new BigDecimal("100")));
        position.setCurrentPrice(new BigDecimal("110"));
        position.setLastUpdated(LocalDate.now());
        return position;
    }
}

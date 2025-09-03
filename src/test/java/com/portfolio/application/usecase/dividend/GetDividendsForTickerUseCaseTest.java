package com.portfolio.application.usecase.dividend;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Dividend;
import com.portfolio.domain.port.MarketDataService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetDividendsForTickerUseCaseTest {
    private MarketDataService marketDataService;
    private GetDividendsForTickerUseCase getDividendsUseCase;

    @BeforeEach
    void setUp() {
        marketDataService = mock(MarketDataService.class);
        getDividendsUseCase = new GetDividendsForTickerUseCase();
        getDividendsUseCase.marketDataService = marketDataService;
    }

    @Test
    void testExecuteSuccess() {
        // Given
        String ticker = "AAPL";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        Dividend dividend1 = new Dividend("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 5, 12), new BigDecimal("0.24"));
        Dividend dividend2 = new Dividend("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 8, 11), new BigDecimal("0.24"));
        List<Dividend> expectedDividends = Arrays.asList(dividend1, dividend2);
        
        when(marketDataService.getDividends("AAPL", startDate, endDate))
            .thenReturn(Uni.createFrom().item(expectedDividends));

        // When
        Uni<List<Dividend>> uni = getDividendsUseCase.execute(ticker, startDate, endDate);
        List<Dividend> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals(new BigDecimal("0.24"), result.get(0).getAmount());
        
        verify(marketDataService).getDividends("AAPL", startDate, endDate);
    }

    @Test
    void testExecuteWithEmptyResult() {
        // Given
        String ticker = "NONDIV";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        when(marketDataService.getDividends("NONDIV", startDate, endDate))
            .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        // When
        Uni<List<Dividend>> uni = getDividendsUseCase.execute(ticker, startDate, endDate);
        List<Dividend> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(marketDataService).getDividends("NONDIV", startDate, endDate);
    }



    @Test
    void testExecuteWithMarketDataServiceFailure() {
        // Given
        String ticker = "FAIL";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        RuntimeException marketDataException = new RuntimeException("Market data service error");
        
        when(marketDataService.getDividends("FAIL", startDate, endDate))
            .thenReturn(Uni.createFrom().failure(marketDataException));

        // When
        Uni<List<Dividend>> uni = getDividendsUseCase.execute(ticker, startDate, endDate);
        UniAssertSubscriber<List<Dividend>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetDividends.MARKET_DATA_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("Failed to retrieve dividends for ticker: FAIL"));
        assertEquals(marketDataException, thrown.getCause());
        
        verify(marketDataService).getDividends("FAIL", startDate, endDate);
    }

    @Test
    void testExecuteNormalizesTickerInput() {
        // Given
        String ticker = "  aapl  "; // lowercase with whitespace
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        when(marketDataService.getDividends("AAPL", startDate, endDate))
            .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        // When
        Uni<List<Dividend>> uni = getDividendsUseCase.execute(ticker, startDate, endDate);
        List<Dividend> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        verify(marketDataService).getDividends("AAPL", startDate, endDate); // Verify normalized ticker
    }


}

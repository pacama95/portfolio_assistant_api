package com.portfolio.infrastructure.rest;

import com.portfolio.application.usecase.dividend.GetDividendsForPortfolioUseCase;
import com.portfolio.application.usecase.dividend.GetDividendsForTickerUseCase;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.model.Dividend;
import com.portfolio.infrastructure.rest.dto.DividendResponse;
import com.portfolio.infrastructure.rest.mapper.DividendMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DividendControllerTest {
    private GetDividendsForTickerUseCase getDividendsForTickerUseCase;
    private GetDividendsForPortfolioUseCase getDividendsForPortfolioUseCase;
    private DividendMapper dividendMapper;
    private DividendController dividendController;

    @BeforeEach
    void setUp() {
        getDividendsForTickerUseCase = mock(GetDividendsForTickerUseCase.class);
        getDividendsForPortfolioUseCase = mock(GetDividendsForPortfolioUseCase.class);
        dividendMapper = mock(DividendMapper.class);
        dividendController = new DividendController();
        dividendController.getDividendsForTickerUseCase = getDividendsForTickerUseCase;
        dividendController.getDividendsForPortfolioUseCase = getDividendsForPortfolioUseCase;
        dividendController.dividendMapper = dividendMapper;
    }

    @Test
    void testGetDividendsForTickerSuccess() {
        // Given
        String ticker = "AAPL";
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-12-31";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        Dividend dividend = new Dividend("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 5, 12), new BigDecimal("0.24"));
        List<Dividend> dividends = List.of(dividend);
        
        DividendResponse response = new DividendResponse("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 5, 12), new BigDecimal("0.24"));
        List<DividendResponse> responses = List.of(response);
        
        when(getDividendsForTickerUseCase.execute(ticker, startDate, endDate))
            .thenReturn(Uni.createFrom().item(dividends));
        when(dividendMapper.toResponses(dividends)).thenReturn(responses);

        // When
        Uni<Response> uni = dividendController.getDividendsForTicker(ticker, startDateStr, endDateStr);
        Response result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(200, result.getStatus());
        assertEquals(responses, result.getEntity());
        
        verify(getDividendsForTickerUseCase).execute(ticker, startDate, endDate);
        verify(dividendMapper).toResponses(dividends);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "null"})
    void testGetDividendsForTickerWithInvalidTicker(String invalidTicker) {
        // Given
        String ticker = "null".equals(invalidTicker) ? null : invalidTicker;
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-12-31";

        // When
        Uni<Response> uni = dividendController.getDividendsForTicker(ticker, startDateStr, endDateStr);
        Response result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(400, result.getStatus());
        assertTrue(result.getEntity().toString().contains("Ticker symbol is required"));
        
        verifyNoInteractions(getDividendsForTickerUseCase);
        verifyNoInteractions(dividendMapper);
    }

    @ParameterizedTest
    @MethodSource("invalidDateParams")
    void testGetDividendsForTickerWithInvalidDates(String startDate, String endDate, String expectedMessage) {
        // Given
        String ticker = "AAPL";

        // When
        Uni<Response> uni = dividendController.getDividendsForTicker(ticker, startDate, endDate);
        Response result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(400, result.getStatus());
        assertTrue(result.getEntity().toString().contains(expectedMessage));
        
        verifyNoInteractions(getDividendsForTickerUseCase);
        verifyNoInteractions(dividendMapper);
    }

    private static Stream<Arguments> invalidDateParams() {
        return Stream.of(
            Arguments.of(null, "2023-12-31", "Both startDate and endDate query parameters are required"),
            Arguments.of("2023-01-01", null, "Both startDate and endDate query parameters are required"),
            Arguments.of(null, null, "Both startDate and endDate query parameters are required"),
            Arguments.of("invalid-date", "2023-12-31", "Invalid date format"),
            Arguments.of("2023-01-01", "invalid-date", "Invalid date format"),
            Arguments.of("2023-12-31", "2023-01-01", "Start date cannot be after end date")
        );
    }

    @Test
    void testGetDividendsForTickerWithUseCaseFailure() {
        // Given
        String ticker = "FAIL";
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-12-31";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        ServiceException serviceException = new ServiceException(Errors.GetDividends.MARKET_DATA_ERROR, "Market data error");
        
        when(getDividendsForTickerUseCase.execute(ticker, startDate, endDate))
            .thenReturn(Uni.createFrom().failure(serviceException));

        // When
        Uni<Response> uni = dividendController.getDividendsForTicker(ticker, startDateStr, endDateStr);
        Response result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(500, result.getStatus());
        assertTrue(result.getEntity().toString().contains("Error retrieving dividends"));
        
        verify(getDividendsForTickerUseCase).execute(ticker, startDate, endDate);
        verifyNoInteractions(dividendMapper);
    }

    @Test
    void testGetDividendsForPortfolioSuccess() {
        // Given
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-12-31";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        Dividend aaplDividend = new Dividend("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 5, 12), new BigDecimal("0.24"));
        Dividend msftDividend = new Dividend("MSFT", "XNAS", "NASDAQ", LocalDate.of(2023, 6, 14), new BigDecimal("0.75"));
        
        Map<String, List<Dividend>> dividendsMap = Map.of(
            "AAPL", List.of(aaplDividend),
            "MSFT", List.of(msftDividend)
        );
        
        DividendResponse aaplResponse = new DividendResponse("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 5, 12), new BigDecimal("0.24"));
        DividendResponse msftResponse = new DividendResponse("MSFT", "XNAS", "NASDAQ", LocalDate.of(2023, 6, 14), new BigDecimal("0.75"));
        
        Map<String, List<DividendResponse>> responseMap = Map.of(
            "AAPL", List.of(aaplResponse),
            "MSFT", List.of(msftResponse)
        );
        
        when(getDividendsForPortfolioUseCase.execute(startDate, endDate))
            .thenReturn(Uni.createFrom().item(dividendsMap));
        when(dividendMapper.toResponseMap(dividendsMap)).thenReturn(responseMap);

        // When
        Uni<Response> uni = dividendController.getDividendsForPortfolio(startDateStr, endDateStr);
        Response result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(200, result.getStatus());
        assertEquals(responseMap, result.getEntity());
        
        verify(getDividendsForPortfolioUseCase).execute(startDate, endDate);
        verify(dividendMapper).toResponseMap(dividendsMap);
    }

    @ParameterizedTest
    @MethodSource("invalidPortfolioDateParams")
    void testGetDividendsForPortfolioWithInvalidDates(String startDate, String endDate, String expectedMessage) {
        // When
        Uni<Response> uni = dividendController.getDividendsForPortfolio(startDate, endDate);
        Response result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(400, result.getStatus());
        assertTrue(result.getEntity().toString().contains(expectedMessage));
        
        verifyNoInteractions(getDividendsForPortfolioUseCase);
        verifyNoInteractions(dividendMapper);
    }

    private static Stream<Arguments> invalidPortfolioDateParams() {
        return Stream.of(
            Arguments.of(null, "2023-12-31", "Both startDate and endDate query parameters are required"),
            Arguments.of("2023-01-01", null, "Both startDate and endDate query parameters are required"),
            Arguments.of(null, null, "Both startDate and endDate query parameters are required"),
            Arguments.of("invalid-date", "2023-12-31", "Invalid date format"),
            Arguments.of("2023-01-01", "invalid-date", "Invalid date format"),
            Arguments.of("2023-12-31", "2023-01-01", "Start date cannot be after end date")
        );
    }

    @Test
    void testGetDividendsForPortfolioWithUseCaseFailure() {
        // Given
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-12-31";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        ServiceException serviceException = new ServiceException(Errors.GetDividends.PERSISTENCE_ERROR, "Database error");
        
        when(getDividendsForPortfolioUseCase.execute(startDate, endDate))
            .thenReturn(Uni.createFrom().failure(serviceException));

        // When
        Uni<Response> uni = dividendController.getDividendsForPortfolio(startDateStr, endDateStr);
        Response result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(500, result.getStatus());
        assertTrue(result.getEntity().toString().contains("Error retrieving portfolio dividends"));
        
        verify(getDividendsForPortfolioUseCase).execute(startDate, endDate);
        verifyNoInteractions(dividendMapper);
    }

    @Test
    void testGetDividendsForPortfolioWithEmptyResult() {
        // Given
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-12-31";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        Map<String, List<Dividend>> emptyDividendsMap = Collections.emptyMap();
        Map<String, List<DividendResponse>> emptyResponseMap = Collections.emptyMap();
        
        when(getDividendsForPortfolioUseCase.execute(startDate, endDate))
            .thenReturn(Uni.createFrom().item(emptyDividendsMap));
        when(dividendMapper.toResponseMap(emptyDividendsMap)).thenReturn(emptyResponseMap);

        // When
        Uni<Response> uni = dividendController.getDividendsForPortfolio(startDateStr, endDateStr);
        Response result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(200, result.getStatus());
        assertEquals(emptyResponseMap, result.getEntity());
        
        verify(getDividendsForPortfolioUseCase).execute(startDate, endDate);
        verify(dividendMapper).toResponseMap(emptyDividendsMap);
    }

    @Test
    void testGetDividendsForTickerWithEmptyResult() {
        // Given
        String ticker = "NONDIV";
        String startDateStr = "2023-01-01";
        String endDateStr = "2023-12-31";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        List<Dividend> emptyDividends = Collections.emptyList();
        List<DividendResponse> emptyResponses = Collections.emptyList();
        
        when(getDividendsForTickerUseCase.execute(ticker, startDate, endDate))
            .thenReturn(Uni.createFrom().item(emptyDividends));
        when(dividendMapper.toResponses(emptyDividends)).thenReturn(emptyResponses);

        // When
        Uni<Response> uni = dividendController.getDividendsForTicker(ticker, startDateStr, endDateStr);
        Response result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(200, result.getStatus());
        assertEquals(emptyResponses, result.getEntity());
        
        verify(getDividendsForTickerUseCase).execute(ticker, startDate, endDate);
        verify(dividendMapper).toResponses(emptyDividends);
    }
}

package com.portfolio.infrastructure.marketdata.adapter;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Dividend;
import com.portfolio.infrastructure.marketdata.client.TwelveDataClient;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataDividendResponse;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataDividendsMeta;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataDividendsWrapper;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataPriceResponse;
import com.portfolio.infrastructure.marketdata.mapper.DividendMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwelveDataMarketDataServiceTest {
    private TwelveDataClient twelveDataClient;
    private DividendMapper dividendMapper;
    private TwelveDataMarketDataService marketDataService;
    private static final String TEST_API_KEY = "test-api-key-12345";

    @BeforeEach
    void setUp() {
        twelveDataClient = mock(TwelveDataClient.class);
        dividendMapper = mock(DividendMapper.class);
        marketDataService = new TwelveDataMarketDataService(twelveDataClient, dividendMapper);
        marketDataService.apiKey = TEST_API_KEY;
    }

    // Helper methods for creating test data
    private TwelveDataDividendsMeta createMeta(String symbol, String name, String currency, String exchange, String micCode, String timezone) {
        TwelveDataDividendsMeta meta = new TwelveDataDividendsMeta();
        meta.setSymbol(symbol);
        meta.setName(name);
        meta.setCurrency(currency);
        meta.setExchange(exchange);
        meta.setMicCode(micCode);
        meta.setExchangeTimezone(timezone);
        return meta;
    }

    private TwelveDataDividendResponse createDividend(LocalDate exDate, BigDecimal amount) {
        TwelveDataDividendResponse dividend = new TwelveDataDividendResponse();
        dividend.setExDate(exDate);
        dividend.setAmount(amount);
        return dividend;
    }

    private TwelveDataDividendsWrapper createWrapper(TwelveDataDividendsMeta meta, List<TwelveDataDividendResponse> dividends) {
        TwelveDataDividendsWrapper wrapper = new TwelveDataDividendsWrapper();
        wrapper.setMeta(meta);
        wrapper.setDividends(dividends);
        return wrapper;
    }

    @Test
    void testGetCurrentPriceSuccess() {
        // Given
        String ticker = "AAPL";
        BigDecimal expectedPrice = new BigDecimal("175.50");
        TwelveDataPriceResponse response = new TwelveDataPriceResponse();
        response.setPrice(expectedPrice);
        
        when(twelveDataClient.getPrice(ticker, TEST_API_KEY))
            .thenReturn(Uni.createFrom().item(response));

        // When
        Uni<BigDecimal> uni = marketDataService.getCurrentPrice(ticker);
        BigDecimal result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(expectedPrice, result);
        verify(twelveDataClient).getPrice(ticker, TEST_API_KEY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "null"})
    void testGetCurrentPriceWithInvalidTicker(String invalidTicker) {
        // Given
        String ticker = "null".equals(invalidTicker) ? null : invalidTicker;
        
        // When
        Uni<BigDecimal> uni = marketDataService.getCurrentPrice(ticker);
        UniAssertSubscriber<BigDecimal> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.MarketData.INVALID_INPUT, thrown.getError());
        assertTrue(thrown.getMessage().contains("Ticker symbol cannot be null or empty"));
        verifyNoInteractions(twelveDataClient);
    }

    @Test
    void testGetCurrentPriceWithGenericApiFailure() {
        // Given
        String ticker = "AAPL";
        RuntimeException clientException = new RuntimeException("API rate limit exceeded");
        
        when(twelveDataClient.getPrice("AAPL", TEST_API_KEY))
            .thenReturn(Uni.createFrom().failure(clientException));

        // When
        Uni<BigDecimal> uni = marketDataService.getCurrentPrice(ticker);
        UniAssertSubscriber<BigDecimal> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.MarketData.API_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("Failed to fetch price for ticker: AAPL"));
        assertEquals(clientException, thrown.getCause());
        verify(twelveDataClient).getPrice("AAPL", TEST_API_KEY);
    }

    @Test
    void testGetCurrentPriceWithNetworkError() {
        // Given
        String ticker = "MSFT";
        RuntimeException networkException = new RuntimeException("Connection timeout");
        
        when(twelveDataClient.getPrice("MSFT", TEST_API_KEY))
            .thenReturn(Uni.createFrom().failure(networkException));

        // When
        Uni<BigDecimal> uni = marketDataService.getCurrentPrice(ticker);
        UniAssertSubscriber<BigDecimal> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.MarketData.NETWORK_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("Network error while fetching price for MSFT"));
        assertEquals(networkException, thrown.getCause());
        verify(twelveDataClient).getPrice("MSFT", TEST_API_KEY);
    }

    @Test
    void testGetCurrentPriceWithInvalidTickerWebException() {
        // Given
        String ticker = "INVALID";
        WebApplicationException webException = new WebApplicationException("Invalid ticker", 404);
        
        when(twelveDataClient.getPrice("INVALID", TEST_API_KEY))
            .thenReturn(Uni.createFrom().failure(webException));

        // When
        Uni<BigDecimal> uni = marketDataService.getCurrentPrice(ticker);
        UniAssertSubscriber<BigDecimal> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.MarketData.INVALID_TICKER, thrown.getError());
        assertTrue(thrown.getMessage().contains("Invalid ticker symbol"));
        assertEquals(webException, thrown.getCause());
        verify(twelveDataClient).getPrice("INVALID", TEST_API_KEY);
    }

    @Test
    void testGetCurrentPriceWithServerErrorWebException() {
        // Given
        String ticker = "SERVER_ERROR";
        WebApplicationException webException = new WebApplicationException("Server error", 500);
        
        when(twelveDataClient.getPrice("SERVER_ERROR", TEST_API_KEY))
            .thenReturn(Uni.createFrom().failure(webException));

        // When
        Uni<BigDecimal> uni = marketDataService.getCurrentPrice(ticker);
        UniAssertSubscriber<BigDecimal> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.MarketData.API_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("server error"));
        assertEquals(webException, thrown.getCause());
        verify(twelveDataClient).getPrice("SERVER_ERROR", TEST_API_KEY);
    }

    @Test
    void testGetCurrentPriceWithBadRequestWebException() {
        // Given
        String ticker = "BAD_REQUEST";
        WebApplicationException webException = new WebApplicationException("Bad request", 400);
        
        when(twelveDataClient.getPrice("BAD_REQUEST", TEST_API_KEY))
            .thenReturn(Uni.createFrom().failure(webException));

        // When
        Uni<BigDecimal> uni = marketDataService.getCurrentPrice(ticker);
        UniAssertSubscriber<BigDecimal> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.MarketData.INVALID_TICKER, thrown.getError());
        assertTrue(thrown.getMessage().contains("Invalid ticker symbol"));
        assertEquals(webException, thrown.getCause());
        verify(twelveDataClient).getPrice("BAD_REQUEST", TEST_API_KEY);
    }

    @Test
    void testGetCurrentPriceWithNullResponse() {
        // Given
        String ticker = "NULL_RESPONSE";
        
        when(twelveDataClient.getPrice("NULL_RESPONSE", TEST_API_KEY))
            .thenReturn(Uni.createFrom().item((TwelveDataPriceResponse) null));

        // When
        Uni<BigDecimal> uni = marketDataService.getCurrentPrice(ticker);
        UniAssertSubscriber<BigDecimal> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.MarketData.NULL_RESPONSE, thrown.getError());
        assertEquals("API returned null response", thrown.getMessage());
        verify(twelveDataClient).getPrice("NULL_RESPONSE", TEST_API_KEY);
    }

    @Test
    void testGetCurrentPriceWithNullPrice() {
        // Given
        String ticker = "NULL_PRICE";
        TwelveDataPriceResponse response = new TwelveDataPriceResponse();
        response.setPrice(null);
        
        when(twelveDataClient.getPrice("NULL_PRICE", TEST_API_KEY))
            .thenReturn(Uni.createFrom().item(response));

        // When
        Uni<BigDecimal> uni = marketDataService.getCurrentPrice(ticker);
        UniAssertSubscriber<BigDecimal> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.MarketData.NULL_RESPONSE, thrown.getError());
        assertEquals("API returned null price", thrown.getMessage());
        verify(twelveDataClient).getPrice("NULL_PRICE", TEST_API_KEY);
    }

    @Test
    void testGetCurrentPriceNormalizesTickerInput() {
        // Given
        String ticker = "  aapl  "; // lowercase with whitespace
        BigDecimal expectedPrice = new BigDecimal("175.50");
        TwelveDataPriceResponse response = new TwelveDataPriceResponse();
        response.setPrice(expectedPrice);
        
        when(twelveDataClient.getPrice("AAPL", TEST_API_KEY))
            .thenReturn(Uni.createFrom().item(response));

        // When
        Uni<BigDecimal> uni = marketDataService.getCurrentPrice(ticker);
        BigDecimal result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(expectedPrice, result);
        verify(twelveDataClient).getPrice("AAPL", TEST_API_KEY); // Verify normalized ticker
    }

    @Test
    void testGetCurrentPriceRethrowsServiceException() {
        // Given
        String ticker = "SERVICE_EX";
        ServiceException originalException = new ServiceException(Errors.MarketData.API_ERROR, "Original service exception");
        
        when(twelveDataClient.getPrice("SERVICE_EX", TEST_API_KEY))
            .thenReturn(Uni.createFrom().failure(originalException));

        // When
        Uni<BigDecimal> uni = marketDataService.getCurrentPrice(ticker);
        UniAssertSubscriber<BigDecimal> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertSame(originalException, thrown); // Should be the same instance, not wrapped
        verify(twelveDataClient).getPrice("SERVICE_EX", TEST_API_KEY);
    }

    // ===================
    // DIVIDEND TESTS
    // ===================

    @Test
    void testGetDividendsSuccess() {
        // Given
        String ticker = "AAPL";
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        TwelveDataDividendsMeta meta = createMeta("AAPL", "Apple Inc.", "USD", "NASDAQ", "XNGS", "America/New_York");
        TwelveDataDividendResponse dividend1 = createDividend(LocalDate.of(2023, 11, 10), new BigDecimal("0.24"));
        TwelveDataDividendResponse dividend2 = createDividend(LocalDate.of(2023, 8, 11), new BigDecimal("0.24"));
        TwelveDataDividendsWrapper wrapper = createWrapper(meta, Arrays.asList(dividend1, dividend2));
        
        // Create expected domain objects
        Dividend expectedDividend1 = new Dividend("AAPL", "XNGS", "NASDAQ", LocalDate.of(2023, 11, 10), new BigDecimal("0.24"));
        Dividend expectedDividend2 = new Dividend("AAPL", "XNGS", "NASDAQ", LocalDate.of(2023, 8, 11), new BigDecimal("0.24"));
        List<Dividend> expectedDividends = Arrays.asList(expectedDividend1, expectedDividend2);
        
        when(twelveDataClient.getDividends("AAPL", "2020-01-01", "2023-12-31", TEST_API_KEY))
            .thenReturn(Uni.createFrom().item(wrapper));
        when(dividendMapper.toDomain(wrapper)).thenReturn(expectedDividends);

        // When
        Uni<List<Dividend>> uni = marketDataService.getDividends(ticker, startDate, endDate);
        List<Dividend> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        Dividend first = result.get(0);
        assertEquals("AAPL", first.getSymbol());
        assertEquals("XNGS", first.getMicCode());
        assertEquals("NASDAQ", first.getExchange());
        assertEquals(LocalDate.of(2023, 11, 10), first.getExDate());
        assertEquals(new BigDecimal("0.24"), first.getAmount());
        
        verify(twelveDataClient).getDividends("AAPL", "2020-01-01", "2023-12-31", TEST_API_KEY);
        verify(dividendMapper).toDomain(wrapper);
    }

    @Test
    void testGetDividendsWithEmptyResult() {
        // Given
        String ticker = "NONDIV";
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        TwelveDataDividendsMeta meta = createMeta("NONDIV", "No Dividend Corp.", "USD", "NASDAQ", "XNGS", "America/New_York");
        TwelveDataDividendsWrapper wrapper = createWrapper(meta, Collections.emptyList());
        
        when(twelveDataClient.getDividends("NONDIV", "2020-01-01", "2023-12-31", TEST_API_KEY))
            .thenReturn(Uni.createFrom().item(wrapper));
        when(dividendMapper.toDomain(wrapper)).thenReturn(Collections.emptyList());

        // When
        Uni<List<Dividend>> uni = marketDataService.getDividends(ticker, startDate, endDate);
        List<Dividend> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(twelveDataClient).getDividends("NONDIV", "2020-01-01", "2023-12-31", TEST_API_KEY);
        verify(dividendMapper).toDomain(wrapper);
    }

    @Test
    void testGetDividendsWithNullResponse() {
        // Given
        String ticker = "NULL_RESPONSE";
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        when(twelveDataClient.getDividends("NULL_RESPONSE", "2020-01-01", "2023-12-31", TEST_API_KEY))
            .thenReturn(Uni.createFrom().nullItem());

        // When
        Uni<List<Dividend>> uni = marketDataService.getDividends(ticker, startDate, endDate);
        UniAssertSubscriber<List<Dividend>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.MarketData.NULL_RESPONSE, thrown.getError());
        assertEquals("API returned null response", thrown.getMessage());
        verify(twelveDataClient).getDividends("NULL_RESPONSE", "2020-01-01", "2023-12-31", TEST_API_KEY);
    }

    @Test
    void testGetDividendsWithWebApplicationException() {
        // Given
        String ticker = "INVALID";
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        WebApplicationException webException = new WebApplicationException("Invalid ticker", 404);
        
        when(twelveDataClient.getDividends("INVALID", "2020-01-01", "2023-12-31", TEST_API_KEY))
            .thenReturn(Uni.createFrom().failure(webException));

        // When
        Uni<List<Dividend>> uni = marketDataService.getDividends(ticker, startDate, endDate);
        UniAssertSubscriber<List<Dividend>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.MarketData.INVALID_TICKER, thrown.getError());
        assertTrue(thrown.getMessage().contains("Invalid ticker symbol"));
        assertEquals(webException, thrown.getCause());
        verify(twelveDataClient).getDividends("INVALID", "2020-01-01", "2023-12-31", TEST_API_KEY);
    }

    @Test
    void testGetDividendsWithNetworkError() {
        // Given
        String ticker = "NETWORK_ERROR";
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        RuntimeException networkException = new RuntimeException("Connection timeout");
        
        when(twelveDataClient.getDividends("NETWORK_ERROR", "2020-01-01", "2023-12-31", TEST_API_KEY))
            .thenReturn(Uni.createFrom().failure(networkException));

        // When
        Uni<List<Dividend>> uni = marketDataService.getDividends(ticker, startDate, endDate);
        UniAssertSubscriber<List<Dividend>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.MarketData.NETWORK_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("Network error while fetching dividends for NETWORK_ERROR"));
        assertEquals(networkException, thrown.getCause());
        verify(twelveDataClient).getDividends("NETWORK_ERROR", "2020-01-01", "2023-12-31", TEST_API_KEY);
    }

    @Test
    void testGetDividendsNormalizesTickerInput() {
        // Given
        String ticker = "  aapl  "; // lowercase with whitespace
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        TwelveDataDividendsMeta meta = createMeta("AAPL", "Apple Inc.", "USD", "NASDAQ", "XNGS", "America/New_York");
        TwelveDataDividendsWrapper wrapper = createWrapper(meta, Collections.emptyList());
        
        when(twelveDataClient.getDividends("AAPL", "2020-01-01", "2023-12-31", TEST_API_KEY))
            .thenReturn(Uni.createFrom().item(wrapper));
        when(dividendMapper.toDomain(wrapper)).thenReturn(Collections.emptyList());

        // When
        Uni<List<Dividend>> uni = marketDataService.getDividends(ticker, startDate, endDate);
        List<Dividend> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        verify(twelveDataClient).getDividends("AAPL", "2020-01-01", "2023-12-31", TEST_API_KEY); // Verify normalized ticker
        verify(dividendMapper).toDomain(wrapper);
    }

    @Test
    void testGetDividendsRethrowsServiceException() {
        // Given
        String ticker = "SERVICE_EX";
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        ServiceException originalException = new ServiceException(Errors.MarketData.API_ERROR, "Original service exception");
        
        when(twelveDataClient.getDividends("SERVICE_EX", "2020-01-01", "2023-12-31", TEST_API_KEY))
            .thenReturn(Uni.createFrom().failure(originalException));

        // When
        Uni<List<Dividend>> uni = marketDataService.getDividends(ticker, startDate, endDate);
        UniAssertSubscriber<List<Dividend>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertSame(originalException, thrown); // Should be the same instance, not wrapped
        verify(twelveDataClient).getDividends("SERVICE_EX", "2020-01-01", "2023-12-31", TEST_API_KEY);
    }
}

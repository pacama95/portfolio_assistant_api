package com.portfolio.infrastructure.marketdata.adapter;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Dividend;
import com.portfolio.domain.port.MarketDataService;
import com.portfolio.infrastructure.marketdata.client.TwelveDataClient;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataDividendsWrapper;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataPriceResponse;
import com.portfolio.infrastructure.marketdata.mapper.DividendMapper;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TwelveData implementation of MarketDataService port
 */
@ApplicationScoped
@Slf4j
public class TwelveDataMarketDataService implements MarketDataService {

    private final TwelveDataClient twelveDataClient;
    private final DividendMapper dividendMapper;

    @ConfigProperty(name = "application.market-data.twelve-data.api-key")
    String apiKey;

    public TwelveDataMarketDataService(@RestClient TwelveDataClient twelveDataClient, 
                                       DividendMapper dividendMapper) {
        this.twelveDataClient = twelveDataClient;
        this.dividendMapper = dividendMapper;
    }


    /**
     * Gets current price for a ticker with caching
     * Cache key: "stock-price-{ticker}"
     * Cache name: "stock-prices"  
     * Cache expiry and eviction configured in application.properties
     * 
     * @param ticker the stock ticker symbol
     * @return the current price as BigDecimal
     * @throws ServiceException if ticker is invalid, API fails, or response is null
     */
    @Override
    @CacheResult(cacheName = "stock-prices")
    public Uni<BigDecimal> getCurrentPrice(String ticker) {
        log.info("Fetching current price for ticker: {}", ticker);
        
        // Input validation
        if (ticker == null || ticker.trim().isEmpty()) {
            log.error("Invalid ticker provided: {}", ticker);
            return Uni.createFrom().failure(
                new ServiceException(Errors.MarketData.INVALID_INPUT, "Ticker symbol cannot be null or empty")
            );
        }
        
        return twelveDataClient.getPrice(ticker.trim().toUpperCase(), apiKey)
            .map(this::validateAndExtractPrice)
            .onFailure().transform(throwable -> transformException(ticker, throwable));
    }

    /**
     * Validates the API response and extracts the price
     * 
     * @param response the TwelveData API response
     * @return the price from the response
     * @throws ServiceException if response or price is null
     */
    private BigDecimal validateAndExtractPrice(TwelveDataPriceResponse response) {
        if (response == null) {
            log.error("Received null response from TwelveData API");
            throw new ServiceException(Errors.MarketData.NULL_RESPONSE, "API returned null response");
        }
        
        BigDecimal price = response.getPrice();
        if (price == null) {
            log.error("Received null price in response from TwelveData API");
            throw new ServiceException(Errors.MarketData.NULL_RESPONSE, "API returned null price");
        }
        
        log.info("Successfully retrieved price: {}", price);
        return price;
    }

    /**
     * Gets dividends for a stock ticker within a date range with caching
     * Cache key: "dividends-{ticker}-{startDate}-{endDate}"
     * Cache name: "dividends"
     * 
     * @param ticker the stock ticker symbol
     * @param startDate the start date for dividend query
     * @param endDate the end date for dividend query
     * @return list of dividends for the specified period
     * @throws ServiceException if ticker is invalid, dates are invalid, API fails, or response is null
     */
    @Override
    @CacheResult(cacheName = "dividends")
    public Uni<List<Dividend>> getDividends(String ticker, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching dividends for ticker: {} from {} to {}", ticker, startDate, endDate);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);
        
        return twelveDataClient.getDividends(ticker.trim().toUpperCase(), formattedStartDate, formattedEndDate, apiKey)
            .map(this::validateAndMapDividends)
            .onFailure().transform(throwable -> transformDividendsException(ticker, throwable));
    }

    /**
     * Validates the API response and maps dividends to domain models
     * 
     * @param wrapper the TwelveData API response wrapper containing meta and dividends
     * @return the list of domain Dividend models
     * @throws ServiceException if response is null
     */
    private List<Dividend> validateAndMapDividends(TwelveDataDividendsWrapper wrapper) {
        if (wrapper == null) {
            log.error("Received null response from TwelveData dividends API");
            throw new ServiceException(Errors.MarketData.NULL_RESPONSE, "API returned null response");
        }
        
        if (wrapper.getDividends() == null) {
            log.info("No dividends found for the specified period");
            return List.of();
        }
        
        List<Dividend> dividends = dividendMapper.toDomain(wrapper);
        log.info("Successfully retrieved {} dividends", dividends.size());
        return dividends;
    }

    /**
     * Transforms various exceptions into appropriate ServiceException instances for dividends
     * 
     * @param ticker the ticker symbol being processed
     * @param throwable the original exception
     * @return ServiceException with appropriate error code and message
     */
    private ServiceException transformDividendsException(String ticker, Throwable throwable) {
        log.error("Failed to fetch dividends for ticker {}: {}", ticker, throwable.getMessage(), throwable);
        
        if (throwable instanceof ServiceException) {
            // Already a ServiceException, return as-is
            return (ServiceException) throwable;
        } else if (throwable instanceof WebApplicationException webEx) {
            // HTTP-related errors from the REST client
            int statusCode = webEx.getResponse().getStatus();
            if (statusCode == 404 || statusCode == 400) {
                return new ServiceException(Errors.MarketData.INVALID_TICKER, 
                    "Invalid ticker symbol: " + ticker, throwable);
            } else if (statusCode >= 500) {
                return new ServiceException(Errors.MarketData.API_ERROR, 
                    "TwelveData API server error (status: " + statusCode + ")", throwable);
            } else {
                return new ServiceException(Errors.MarketData.API_ERROR, 
                    "TwelveData API error (status: " + statusCode + ")", throwable);
            }
        } else if (throwable.getMessage() != null && 
                   (throwable.getMessage().contains("timeout") || 
                    throwable.getMessage().contains("connection") ||
                    throwable.getMessage().contains("network"))) {
            // Network-related errors
            return new ServiceException(Errors.MarketData.NETWORK_ERROR, 
                "Network error while fetching dividends for " + ticker, throwable);
        } else {
            // Generic API error for other cases
            return new ServiceException(Errors.MarketData.API_ERROR, 
                "Failed to fetch dividends for ticker: " + ticker, throwable);
        }
    }

    /**
     * Transforms various exceptions into appropriate ServiceException instances
     * 
     * @param ticker the ticker symbol being processed
     * @param throwable the original exception
     * @return ServiceException with appropriate error code and message
     */
    private ServiceException transformException(String ticker, Throwable throwable) {
        log.error("Failed to fetch price for ticker {}: {}", ticker, throwable.getMessage(), throwable);
        
        if (throwable instanceof ServiceException) {
            // Already a ServiceException, return as-is
            return (ServiceException) throwable;
        } else if (throwable instanceof WebApplicationException webEx) {
            // HTTP-related errors from the REST client
            int statusCode = webEx.getResponse().getStatus();
            if (statusCode == 404 || statusCode == 400) {
                return new ServiceException(Errors.MarketData.INVALID_TICKER, 
                    "Invalid ticker symbol: " + ticker, throwable);
            } else if (statusCode >= 500) {
                return new ServiceException(Errors.MarketData.API_ERROR, 
                    "TwelveData API server error (status: " + statusCode + ")", throwable);
            } else {
                return new ServiceException(Errors.MarketData.API_ERROR, 
                    "TwelveData API error (status: " + statusCode + ")", throwable);
            }
        } else if (throwable.getMessage() != null && 
                   (throwable.getMessage().contains("timeout") || 
                    throwable.getMessage().contains("connection") ||
                    throwable.getMessage().contains("network"))) {
            // Network-related errors
            return new ServiceException(Errors.MarketData.NETWORK_ERROR, 
                "Network error while fetching price for " + ticker, throwable);
        } else {
            // Generic API error for other cases
            return new ServiceException(Errors.MarketData.API_ERROR, 
                "Failed to fetch price for ticker: " + ticker, throwable);
        }
    }
}

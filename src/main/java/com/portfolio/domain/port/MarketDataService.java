package com.portfolio.domain.port;

import com.portfolio.domain.model.Dividend;
import io.smallrye.mutiny.Uni;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Port interface for market data operations
 */
public interface MarketDataService {

    /**
     * Gets the current price for a stock ticker symbol
     * 
     * @param ticker the stock ticker symbol (e.g., "AAPL", "MSFT")
     * @return the current price as BigDecimal
     */
    Uni<BigDecimal> getCurrentPrice(String ticker);

    /**
     * Gets dividends for a stock ticker symbol within a date range
     * 
     * @param ticker the stock ticker symbol (e.g., "AAPL", "MSFT")
     * @param startDate the start date for dividend query
     * @param endDate the end date for dividend query
     * @return list of dividends for the specified period
     */
    Uni<List<Dividend>> getDividends(String ticker, LocalDate startDate, LocalDate endDate);
}

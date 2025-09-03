package com.portfolio.application.usecase.dividend;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Dividend;
import com.portfolio.domain.port.MarketDataService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

/**
 * Use case for retrieving dividends for a specific ticker within a date range
 */
@ApplicationScoped
@Slf4j
public class GetDividendsForTickerUseCase {

    @Inject
    MarketDataService marketDataService;

    /**
     * Gets dividends for a specific ticker within a date range
     * 
     * @param ticker the stock ticker symbol (assumed to be already validated and normalized)
     * @param startDate the start date for dividend query (assumed to be already validated)
     * @param endDate the end date for dividend query (assumed to be already validated)
     * @return list of dividends for the specified ticker and period
     */
    public Uni<List<Dividend>> execute(String ticker, LocalDate startDate, LocalDate endDate) {
        log.info("Getting dividends for ticker: {} from {} to {}", ticker, startDate, endDate);
        
        return marketDataService.getDividends(ticker.trim().toUpperCase(), startDate, endDate)
            .onItem().invoke(dividends -> 
                log.info("Successfully retrieved {} dividends for ticker {}", dividends.size(), ticker))
            .onFailure().transform(throwable -> {
                log.error("Failed to retrieve dividends for ticker {}: {}", ticker, throwable.getMessage(), throwable);
                return new ServiceException(Errors.GetDividends.MARKET_DATA_ERROR, 
                    "Failed to retrieve dividends for ticker: " + ticker, throwable);
            });
    }
}

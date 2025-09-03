package com.portfolio.application.usecase.dividend;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Dividend;
import com.portfolio.domain.port.MarketDataService;
import com.portfolio.domain.port.PositionRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Use case for retrieving dividends for all stocks in the portfolio within a date range
 */
@ApplicationScoped
@Slf4j
public class GetDividendsForPortfolioUseCase {

    @Inject
    PositionRepository positionRepository;

    @Inject
    MarketDataService marketDataService;

    /**
     * Gets dividends for all stocks in the portfolio within a date range
     * 
     * @param startDate the start date for dividend query (assumed to be already validated)
     * @param endDate the end date for dividend query (assumed to be already validated)
     * @return map of ticker to list of dividends for the specified period
     */
    @WithSession
    public Uni<Map<String, List<Dividend>>> execute(LocalDate startDate, LocalDate endDate) {
        log.info("Getting dividends for portfolio from {} to {}", startDate, endDate);

        // Get all active positions (with shares > 0)
        return positionRepository.findAllWithShares()
            .onFailure().transform(throwable -> 
                new ServiceException(Errors.GetDividends.PERSISTENCE_ERROR, 
                    "Failed to retrieve positions", throwable))
            .chain(positions -> {
                if (positions.isEmpty()) {
                    log.info("No active positions found in portfolio");
                    return Uni.createFrom().item(Map.<String, List<Dividend>>of());
                }

                log.info("Found {} active positions in portfolio, fetching dividends", positions.size());
                
                // Get dividends for each ticker in parallel
                return Multi.createFrom().iterable(positions)
                    .onItem().transformToUniAndConcatenate(position -> 
                        getDividendsForPosition(position.getTicker(), startDate, endDate))
                    .collect().asMap(
                        result -> result.ticker(),
                        result -> result.dividends()
                    );
            })
            .onItem().invoke(dividendsMap -> {
                int totalDividends = dividendsMap.values().stream()
                    .mapToInt(List::size)
                    .sum();
                log.info("Successfully retrieved dividends for {} tickers with {} total dividend entries", 
                         dividendsMap.size(), totalDividends);
            });
    }

    /**
     * Helper method to get dividends for a specific position
     */
    private Uni<TickerDividendResult> getDividendsForPosition(String ticker, LocalDate startDate, LocalDate endDate) {
        return marketDataService.getDividends(ticker, startDate, endDate)
            .map(dividends -> new TickerDividendResult(ticker, dividends))
            .onFailure().recoverWithItem(throwable -> {
                log.warn("Failed to retrieve dividends for ticker {}: {}", ticker, throwable.getMessage());
                // Return empty list for this ticker instead of failing the entire operation
                return new TickerDividendResult(ticker, List.of());
            });
    }

    /**
     * Record to hold ticker and its associated dividends
     */
    private record TickerDividendResult(String ticker, List<Dividend> dividends) {}
}

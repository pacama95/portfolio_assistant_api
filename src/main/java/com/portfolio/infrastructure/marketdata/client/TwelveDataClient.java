package com.portfolio.infrastructure.marketdata.client;

import com.portfolio.infrastructure.marketdata.dto.TwelveDataDividendsWrapper;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataPriceResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * REST client for TwelveData API
 */
@RegisterRestClient(configKey = "twelve-data-api")
public interface TwelveDataClient {

    /**
     * Gets the current price for a stock ticker
     * 
     * @param symbol the ticker symbol (e.g., "AAPL")
     * @param apikey the TwelveData API key
     * @return the price response
     */
    @GET
    @Path("/price")
    Uni<TwelveDataPriceResponse> getPrice(
            @QueryParam("symbol") String symbol,
            @QueryParam("apikey") String apikey
    );

    /**
     * Gets dividends for a stock ticker within a date range
     * 
     * @param symbol the ticker symbol (e.g., "AAPL")
     * @param startDate the start date in YYYY-MM-DD format
     * @param endDate the end date in YYYY-MM-DD format
     * @param apikey the TwelveData API key
     * @return TwelveDataDividendsWrapper containing meta and dividends
     */
    @GET
    @Path("/dividends")
    Uni<TwelveDataDividendsWrapper> getDividends(
            @QueryParam("symbol") String symbol,
            @QueryParam("start_date") String startDate,
            @QueryParam("end_date") String endDate,
            @QueryParam("apikey") String apikey
    );
}

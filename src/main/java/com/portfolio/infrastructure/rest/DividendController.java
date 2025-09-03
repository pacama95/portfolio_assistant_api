package com.portfolio.infrastructure.rest;

import com.portfolio.application.usecase.dividend.GetDividendsForPortfolioUseCase;
import com.portfolio.application.usecase.dividend.GetDividendsForTickerUseCase;
import com.portfolio.infrastructure.rest.dto.DividendResponse;
import com.portfolio.infrastructure.rest.mapper.DividendMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * REST controller for dividend operations
 */
@Path("/api/dividends")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Dividends", description = "Dividend information and history operations")
public class DividendController {

    @Inject
    GetDividendsForTickerUseCase getDividendsForTickerUseCase;

    @Inject
    GetDividendsForPortfolioUseCase getDividendsForPortfolioUseCase;

    @Inject
    DividendMapper dividendMapper;

    /**
     * Get dividends for a specific ticker within a date range
     */
    @GET
    @Path("/ticker/{ticker}")
    @Operation(summary = "Get dividends for a specific ticker", 
        description = "Retrieves all dividend payments for a specific stock ticker within the specified date range")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "List of dividends for the ticker",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = DividendResponse.class))),
        @APIResponse(responseCode = "400", description = "Invalid ticker or date parameters"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Uni<Response> getDividendsForTicker(
        @Parameter(description = "Stock ticker symbol", required = true, example = "AAPL")
        @PathParam("ticker") String ticker,
        @Parameter(description = "Start date for dividend query (YYYY-MM-DD)", required = true, example = "2023-01-01")
        @QueryParam("startDate") String startDateStr,
        @Parameter(description = "End date for dividend query (YYYY-MM-DD)", required = true, example = "2023-12-31")
        @QueryParam("endDate") String endDateStr) {

        // Validate input parameters
        if (ticker == null || ticker.trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                .entity("Ticker symbol is required")
                .build());
        }

        if (startDateStr == null || endDateStr == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                .entity("Both startDate and endDate query parameters are required")
                .build());
        }

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(startDateStr);
            endDate = LocalDate.parse(endDateStr);
        } catch (DateTimeParseException e) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid date format. Use YYYY-MM-DD format")
                .build());
        }

        if (startDate.isAfter(endDate)) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                .entity("Start date cannot be after end date")
                .build());
        }

        return getDividendsForTickerUseCase.execute(ticker, startDate, endDate)
            .map(dividends -> {
                List<DividendResponse> response = dividendMapper.toResponses(dividends);
                return Response.ok(response).build();
            })
            .onFailure().recoverWithItem(throwable -> 
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving dividends: " + throwable.getMessage())
                    .build()
            );
    }

    /**
     * Get dividends for all stocks in the portfolio within a date range
     */
    @GET
    @Path("/portfolio")
    @Operation(summary = "Get dividends for the entire portfolio", 
        description = "Retrieves all dividend payments for all active positions in the portfolio within the specified date range")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Map of ticker to list of dividends for the portfolio",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.OBJECT))),
        @APIResponse(responseCode = "400", description = "Invalid date parameters"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Uni<Response> getDividendsForPortfolio(
        @Parameter(description = "Start date for dividend query (YYYY-MM-DD)", required = true, example = "2023-01-01")
        @QueryParam("startDate") String startDateStr,
        @Parameter(description = "End date for dividend query (YYYY-MM-DD)", required = true, example = "2023-12-31")
        @QueryParam("endDate") String endDateStr) {

        // Validate input parameters
        if (startDateStr == null || endDateStr == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                .entity("Both startDate and endDate query parameters are required")
                .build());
        }

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(startDateStr);
            endDate = LocalDate.parse(endDateStr);
        } catch (DateTimeParseException e) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid date format. Use YYYY-MM-DD format")
                .build());
        }

        if (startDate.isAfter(endDate)) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                .entity("Start date cannot be after end date")
                .build());
        }

        return getDividendsForPortfolioUseCase.execute(startDate, endDate)
            .map(dividendsMap -> {
                Map<String, List<DividendResponse>> response = dividendMapper.toResponseMap(dividendsMap);
                return Response.ok(response).build();
            })
            .onFailure().recoverWithItem(throwable -> 
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving portfolio dividends: " + throwable.getMessage())
                    .build()
            );
    }
}

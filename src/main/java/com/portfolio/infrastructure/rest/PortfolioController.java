package com.portfolio.infrastructure.rest;

import com.portfolio.application.usecase.portfolio.GetPortfolioSummaryUseCase;
import com.portfolio.infrastructure.rest.dto.PortfolioSummaryResponse;
import com.portfolio.infrastructure.rest.mapper.PortfolioSummaryMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST controller for portfolio summary operations
 */
@Path("/api/portfolio")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Portfolio", description = "Portfolio summary and aggregated data operations")
public class PortfolioController {

    @Inject
    GetPortfolioSummaryUseCase getPortfolioSummaryUseCase;

    @Inject
    PortfolioSummaryMapper portfolioSummaryMapper;

    /**
     * Get complete portfolio summary (all positions)
     */
    @GET
    @Path("/summary")
    @Operation(summary = "Get complete portfolio summary", 
        description = "Retrieves aggregated portfolio data including all positions (active and inactive)")
    @APIResponse(responseCode = "200", description = "Portfolio summary with aggregated financial data",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PortfolioSummaryResponse.class)))
    public Uni<PortfolioSummaryResponse> getPortfolioSummary() {
        return getPortfolioSummaryUseCase.getPortfolioSummary()
            .map(portfolioSummaryMapper::toResponse);
    }

    /**
     * Get active portfolio summary (only positions with shares > 0)
     */
    @GET
    @Path("/summary/active")
    @Operation(summary = "Get active portfolio summary", 
        description = "Retrieves aggregated portfolio data including only active positions (shares > 0)")
    @APIResponse(responseCode = "200", description = "Active portfolio summary with aggregated financial data",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PortfolioSummaryResponse.class)))
    public Uni<PortfolioSummaryResponse> getActivePortfolioSummary() {
        return getPortfolioSummaryUseCase.getActiveSummary()
            .map(portfolioSummaryMapper::toResponse);
    }
} 
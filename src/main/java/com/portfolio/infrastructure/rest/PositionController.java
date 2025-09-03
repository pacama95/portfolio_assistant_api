package com.portfolio.infrastructure.rest;

import com.portfolio.application.usecase.position.GetPositionUseCase;
import com.portfolio.application.usecase.position.RecalculatePositionUseCase;
import com.portfolio.application.usecase.position.UpdateMarketDataUseCase;
import com.portfolio.infrastructure.rest.dto.PositionResponse;
import com.portfolio.infrastructure.rest.dto.UpdateMarketDataRequest;
import com.portfolio.infrastructure.rest.mapper.PositionMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.UUID;

/**
 * REST controller for position management
 */
@Path("/api/positions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Positions", description = "Position management and market data operations")
public class PositionController {

    @Inject
    GetPositionUseCase getPositionUseCase;

    @Inject
    UpdateMarketDataUseCase updateMarketDataUseCase;

    @Inject
    RecalculatePositionUseCase recalculatePositionUseCase;

    @Inject
    PositionMapper positionMapper;

    /**
     * Get all positions
     */
    @GET
    @Operation(summary = "Get all positions", description = "Retrieves all positions in the portfolio, including inactive ones")
    @APIResponse(responseCode = "200", description = "List of all positions",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = PositionResponse.class)))
    public Uni<List<PositionResponse>> getAllPositions() {
        return getPositionUseCase.getAll()
            .map(positionMapper::toCurrentPositionResponses);
    }

    /**
     * Get only active positions (with shares > 0)
     */
    @GET
    @Path("/active")
    @Operation(summary = "Get active positions", description = "Retrieves only positions with shares greater than 0")
    @APIResponse(responseCode = "200", description = "List of active positions",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = PositionResponse.class)))
    public Uni<List<PositionResponse>> getActivePositions() {
        return getPositionUseCase.getActivePositions()
            .map(positionMapper::toCurrentPositionResponses);
    }

    /**
     * Get position by ID
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Get position by ID", description = "Retrieves a specific position by its unique identifier")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Position found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PositionResponse.class))),
        @APIResponse(responseCode = "404", description = "Position not found")
    })
    public Uni<Response> getPosition(
        @Parameter(description = "Position ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathParam("id") UUID id) {
        return getPositionUseCase.getById(id)
            .map(position -> {
                if (position == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                return Response.ok(positionMapper.toResponse(position)).build();
            });
    }

    /**
     * Get position by ticker
     */
    @GET
    @Path("/ticker/{ticker}")
    @Operation(summary = "Get position by ticker", description = "Retrieves the position for a specific stock ticker")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Position found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PositionResponse.class))),
        @APIResponse(responseCode = "404", description = "Position not found")
    })
    public Uni<Response> getPositionByTicker(
        @Parameter(description = "Stock ticker symbol", required = true, example = "AAPL")
        @PathParam("ticker") String ticker) {
        return getPositionUseCase.getByTicker(ticker)
            .map(position -> {
                if (position == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                return Response.ok(positionMapper.toResponse(position)).build();
            });
    }

    /**
     * Update market price for a position
     */
    @PUT
    @Path("/ticker/{ticker}/price")
    @Operation(summary = "Update market price", description = "Updates the current market price for a position")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Market price updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PositionResponse.class))),
        @APIResponse(responseCode = "400", description = "Invalid price data or ticker not found")
    })
    public Uni<Response> updateMarketPrice(
        @Parameter(description = "Stock ticker symbol", required = true, example = "AAPL")
        @PathParam("ticker") String ticker, 
        @Valid UpdateMarketDataRequest request) {
        return updateMarketDataUseCase.execute(ticker, request.price())
            .map(position -> Response.ok(positionMapper.toResponse(position)).build())
            .onFailure().recoverWithItem(throwable -> 
                Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error updating market data: " + throwable.getMessage())
                    .build()
            );
    }

    /**
     * Recalculate position from transactions
     */
    @POST
    @Path("/ticker/{ticker}/recalculate")
    @Operation(summary = "Recalculate position", description = "Recalculates position data based on all transactions for the ticker")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Position recalculated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PositionResponse.class))),
        @APIResponse(responseCode = "404", description = "Position not found"),
        @APIResponse(responseCode = "400", description = "Error during recalculation")
    })
    public Uni<Response> recalculatePosition(
        @Parameter(description = "Stock ticker symbol", required = true, example = "AAPL")
        @PathParam("ticker") String ticker) {
        return recalculatePositionUseCase.execute(ticker)
            .map(position -> {
                if (position == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                return Response.ok(positionMapper.toResponse(position)).build();
            })
            .onFailure().recoverWithItem(throwable -> 
                Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error recalculating position: " + throwable.getMessage())
                    .build()
            );
    }

    /**
     * Check if position exists for ticker
     */
    @GET
    @Path("/ticker/{ticker}/exists")
    @Operation(summary = "Check if position exists", description = "Checks whether a position exists for the given ticker")
    @APIResponse(responseCode = "200", description = "Boolean indicating if position exists",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.BOOLEAN)))
    public Uni<Boolean> checkPositionExists(
        @Parameter(description = "Stock ticker symbol", required = true, example = "AAPL")
        @PathParam("ticker") String ticker) {
        return getPositionUseCase.existsByTicker(ticker);
    }

    /**
     * Get total position count
     */
    @GET
    @Path("/count")
    @Operation(summary = "Get total position count", description = "Returns the total number of positions in the portfolio")
    @APIResponse(responseCode = "200", description = "Total position count",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.INTEGER, format = "int64")))
    public Uni<Long> getPositionCount() {
        return getPositionUseCase.countAll();
    }

    /**
     * Get active position count
     */
    @GET
    @Path("/count/active")
    @Operation(summary = "Get active position count", description = "Returns the number of active positions (with shares > 0)")
    @APIResponse(responseCode = "200", description = "Active position count",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.INTEGER, format = "int64")))
    public Uni<Long> getActivePositionCount() {
        return getPositionUseCase.countActivePositions();
    }
} 
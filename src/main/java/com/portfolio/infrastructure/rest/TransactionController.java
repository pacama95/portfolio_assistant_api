package com.portfolio.infrastructure.rest;

import com.portfolio.application.usecase.transaction.CreateTransactionUseCase;
import com.portfolio.application.usecase.transaction.DeleteTransactionUseCase;
import com.portfolio.application.usecase.transaction.GetTransactionUseCase;
import com.portfolio.application.usecase.transaction.UpdateTransactionUseCase;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.infrastructure.rest.dto.CreateTransactionRequest;
import com.portfolio.infrastructure.rest.dto.TransactionResponse;
import com.portfolio.infrastructure.rest.dto.UpdateTransactionRequest;
import com.portfolio.infrastructure.rest.mapper.TransactionMapper;
import io.smallrye.mutiny.Multi;
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

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller for transaction management
 */
@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Transactions", description = "Transaction management operations")
public class TransactionController {

    @Inject
    CreateTransactionUseCase createTransactionUseCase;

    @Inject
    GetTransactionUseCase getTransactionUseCase;

    @Inject
    UpdateTransactionUseCase updateTransactionUseCase;

    @Inject
    DeleteTransactionUseCase deleteTransactionUseCase;

    @Inject
    TransactionMapper transactionMapper;

    /**
     * Create a new transaction
     */
    @POST
    @Operation(summary = "Create a new transaction", description = "Creates a new buy or sell transaction for a stock")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Transaction created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransactionResponse.class))),
        @APIResponse(responseCode = "400", description = "Invalid request data")
    })
    public Uni<Response> createTransaction(@Valid CreateTransactionRequest request) {
        return Uni.createFrom().item(() -> transactionMapper.toCreateTransactionCommand(request))
                .flatMap(command -> createTransactionUseCase.execute(command))
                .map(transaction -> transactionMapper.toResponse(transaction))
                .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
                .onFailure().recoverWithItem(throwable ->
                    Response.status(Response.Status.BAD_REQUEST)
                       .entity("Error creating transaction: " + throwable.getMessage())
                      .build());
    }

    /**
     * Get transaction by ID
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves a specific transaction by its unique identifier")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Transaction found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransactionResponse.class))),
        @APIResponse(responseCode = "404", description = "Transaction not found")
    })
    public Uni<Response> getTransaction(
        @Parameter(description = "Transaction ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathParam("id") UUID id) {
        return getTransactionUseCase.getById(id)
            .map(transaction -> {
                if (transaction == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                return Response.ok(transactionMapper.toResponse(transaction)).build();
            });
    }

    /**
     * Get all transactions
     */
    @GET
    @Operation(summary = "Get all transactions", description = "Retrieves all transactions in the portfolio")
    @APIResponse(responseCode = "200", description = "List of transactions",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = TransactionResponse.class)))
    public Multi<TransactionResponse> getAllTransactions() {
        return getTransactionUseCase.getAll()
            .map(transactionMapper::toResponse);
    }

    /**
     * Get transactions by ticker
     */
    @GET
    @Path("/ticker/{ticker}")
    @Operation(summary = "Get transactions by ticker", description = "Retrieves all transactions for a specific stock ticker")
    @APIResponse(responseCode = "200", description = "List of transactions for the ticker",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = TransactionResponse.class)))
    public Multi<TransactionResponse> getTransactionsByTicker(
        @Parameter(description = "Stock ticker symbol", required = true, example = "AAPL")
        @PathParam("ticker") String ticker) {
        return getTransactionUseCase.getByTicker(ticker)
            .map(transactionMapper::toResponse);
    }

    /**
     * Search transactions with filters
     */
    @GET
    @Path("/search")
    @Operation(summary = "Search transactions", description = "Search transactions with optional filters")
    @APIResponse(responseCode = "200", description = "List of matching transactions",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = TransactionResponse.class)))
    public Multi<TransactionResponse> searchTransactions(
            @Parameter(description = "Filter by ticker symbol", example = "AAPL")
            @QueryParam("ticker") String ticker,
            @Parameter(description = "Filter by transaction type")
            @QueryParam("type") TransactionType type,
            @Parameter(description = "Filter from date (inclusive)", example = "2023-01-01")
            @QueryParam("fromDate") LocalDate fromDate,
            @Parameter(description = "Filter to date (inclusive)", example = "2023-12-31")
            @QueryParam("toDate") LocalDate toDate) {
        
        return getTransactionUseCase.searchTransactions(ticker, type, fromDate, toDate)
            .map(transactionMapper::toResponse);
    }

    /**
     * Update a transaction
     */
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a transaction", description = "Updates an existing transaction with new data")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Transaction updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransactionResponse.class))),
        @APIResponse(responseCode = "404", description = "Transaction not found"),
        @APIResponse(responseCode = "400", description = "Invalid request data")
    })
    public Uni<Response> updateTransaction(
        @Parameter(description = "Transaction ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathParam("id") UUID id, 
        @Valid UpdateTransactionRequest updateTransactionRequest) {
        return Uni.createFrom().item(() -> transactionMapper.toUpdateTransactionCommand(id, updateTransactionRequest))
                .flatMap(updatedTransaction -> updateTransactionUseCase.execute(updatedTransaction))
                .map(transaction -> {
                    if (transaction == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                    return Response.ok(transactionMapper.toResponse(transaction)).build();
                })
                .onFailure().recoverWithItem(throwable ->
                    Response.status(Response.Status.BAD_REQUEST)
                        .entity("Error updating transaction: " + throwable.getMessage())
                        .build()
                );
    }

    /**
     * Delete a transaction
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a transaction", description = "Permanently deletes a transaction from the portfolio")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Transaction deleted successfully"),
        @APIResponse(responseCode = "404", description = "Transaction not found")
    })
    public Uni<Response> deleteTransaction(
        @Parameter(description = "Transaction ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathParam("id") UUID id) {
        return deleteTransactionUseCase.execute(id)
            .map(deleted -> {
                if (deleted) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            });
    }

    /**
     * Get transaction count
     */
    @GET
    @Path("/count")
    @Operation(summary = "Get total transaction count", description = "Returns the total number of transactions in the portfolio")
    @APIResponse(responseCode = "200", description = "Total transaction count",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.INTEGER, format = "int64")))
    public Uni<Long> getTransactionCount() {
        return getTransactionUseCase.countAll();
    }

    /**
     * Get transaction count by ticker
     */
    @GET
    @Path("/count/{ticker}")
    @Operation(summary = "Get transaction count by ticker", description = "Returns the number of transactions for a specific stock ticker")
    @APIResponse(responseCode = "200", description = "Transaction count for the ticker",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.INTEGER, format = "int64")))
    public Uni<Long> getTransactionCountByTicker(
        @Parameter(description = "Stock ticker symbol", required = true, example = "AAPL")
        @PathParam("ticker") String ticker) {
        return getTransactionUseCase.countByTicker(ticker);
    }
} 
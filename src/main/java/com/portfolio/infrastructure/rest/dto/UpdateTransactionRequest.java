package com.portfolio.infrastructure.rest.dto;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request to update an existing transaction")
public record UpdateTransactionRequest(
    @NotNull(message = "Ticker is required")
    @Size(min = 1, max = 10, message = "Ticker must be between 1 and 10 characters")
    @Schema(description = "Stock ticker symbol", example = "AAPL", required = true)
    String ticker,

    @NotNull(message = "Transaction type is required")
    @Schema(description = "Type of transaction (BUY/SELL)", required = true)
    TransactionType transactionType,

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", inclusive = true, message = "Quantity must be positive")
    @Schema(description = "Number of shares", example = "100.00", required = true)
    BigDecimal quantity,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be positive")
    @Schema(description = "Price per share", example = "150.25", required = true)
    BigDecimal price,

    @DecimalMin(value = "0.0", inclusive = true, message = "Fees cannot be negative")
    @Schema(description = "Transaction fees", example = "9.99")
    BigDecimal fees,

    @NotNull(message = "Currency is required")
    @Schema(description = "Transaction currency", required = true)
    Currency currency,

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    @Schema(description = "Date of the transaction", example = "2023-10-15", required = true)
    LocalDate transactionDate,

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Optional notes about the transaction", example = "Updated investment")
    String notes,

    @Schema(description = "Whether this is a fractional share transaction")
    Boolean isFractional,
    @Schema(description = "Multiplier for fractional shares")
    BigDecimal fractionalMultiplier,
    @Schema(description = "Currency for commission fees")
    Currency commissionCurrency
) {
} 
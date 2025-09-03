package com.portfolio.infrastructure.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Dividend response with dividend payment details")
public record DividendResponse(
    @Schema(description = "Stock ticker symbol", example = "AAPL")
    String symbol,
    @Schema(description = "Market Identifier Code", example = "XNAS")
    String micCode,
    @Schema(description = "Exchange where the stock is traded", example = "NASDAQ")
    String exchange,
    @Schema(description = "Ex-dividend date - date when stock starts trading without dividend", example = "2023-11-10")
    LocalDate exDate,
    @Schema(description = "Dividend amount per share", example = "0.24")
    BigDecimal amount
) {}

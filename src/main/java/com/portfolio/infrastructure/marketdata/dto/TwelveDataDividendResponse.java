package com.portfolio.infrastructure.marketdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for individual dividend item from TwelveData API dividends array
 */
@Data
public class TwelveDataDividendResponse {

    @JsonProperty("ex_date")
    private LocalDate exDate;

    @JsonProperty("amount")
    private BigDecimal amount;
}

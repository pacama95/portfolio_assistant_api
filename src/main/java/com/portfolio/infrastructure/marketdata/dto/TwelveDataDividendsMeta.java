package com.portfolio.infrastructure.marketdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for metadata from TwelveData dividends API response
 */
@Data
public class TwelveDataDividendsMeta {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("name")
    private String name;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("mic_code")
    private String micCode;

    @JsonProperty("exchange_timezone")
    private String exchangeTimezone;
}

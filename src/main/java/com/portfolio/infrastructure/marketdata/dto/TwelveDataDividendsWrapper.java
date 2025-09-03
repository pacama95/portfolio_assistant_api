package com.portfolio.infrastructure.marketdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for the complete TwelveData dividends API response
 */
@Data
public class TwelveDataDividendsWrapper {

    @JsonProperty("meta")
    private TwelveDataDividendsMeta meta;

    @JsonProperty("dividends")
    private List<TwelveDataDividendResponse> dividends;
}

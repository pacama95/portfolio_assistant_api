package com.portfolio.infrastructure.marketdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TwelveDataPriceResponse {

    @JsonProperty("price")
    private BigDecimal price;
}

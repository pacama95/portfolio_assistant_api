package com.portfolio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain model representing a dividend payment for a stock
 */
@Getter
@AllArgsConstructor
public class Dividend {
    
    /**
     * The stock ticker symbol
     */
    private final String symbol;
    
    /**
     * Market Identifier Code
     */
    private final String micCode;
    
    /**
     * Exchange where the stock is traded
     */
    private final String exchange;
    
    /**
     * Ex-dividend date - date when stock starts trading without dividend
     */
    private final LocalDate exDate;
    
    /**
     * Dividend amount per share
     */
    private final BigDecimal amount;
}

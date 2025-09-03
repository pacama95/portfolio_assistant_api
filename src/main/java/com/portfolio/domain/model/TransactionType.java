package com.portfolio.domain.model;

/**
 * Types of transactions supported in the portfolio system
 */
public enum TransactionType {
    BUY("Buy"),
    SELL("Sell"),
    DIVIDEND("Dividend");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 
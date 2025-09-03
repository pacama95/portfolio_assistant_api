package com.portfolio.domain.model;

/**
 * Supported currencies in the portfolio system
 */
public enum Currency {
    USD("US Dollar"),
    EUR("Euro"),
    GBP("British Pound"),
    CAD("Canadian Dollar"),
    JPY("Japanese Yen");

    private final String displayName;

    Currency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 
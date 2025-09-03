package com.portfolio.infrastructure.mcp.converter;

import com.portfolio.domain.model.Currency;
import io.quarkiverse.mcp.server.ToolCallException;
import jakarta.inject.Singleton;

import java.util.Set;

/**
 * Parameter converter strategy for Currency enum types.
 * Handles conversion of currency parameters including transaction and commission currencies.
 */
@Singleton
public class CurrencyParameterConverter implements ParameterConverter {

    // TODO: we could do it more extensible reading the parameters from application.properties
    private static final Set<String> SUPPORTED_PARAMETERS = Set.of(
            "currency", "commissionCurrency", "baseCurrency", 
            "targetCurrency", "fromCurrency", "toCurrency"
    );

    @Override
    public Object convert(Object value, String parameterName) {
        switch (value) {
            case null -> {
                return null;
            }
            case Currency currency -> {
                return currency;
            }
            case String str -> {
                if (str.trim().isEmpty()) {
                    return null;
                }
                try {
                    return Currency.valueOf(str.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new ToolCallException("Invalid Currency value for parameter '" + parameterName +
                            "': " + str + ". Valid values: " + java.util.Arrays.toString(Currency.values()));
                }
            }
            default -> {
            }
        }

        throw new ToolCallException("Cannot convert " + value.getClass().getSimpleName() +
                " to Currency for parameter '" + parameterName + "'");
    }

    @Override
    public boolean matches(String parameterName) {
        return SUPPORTED_PARAMETERS.contains(parameterName);
    }

    @Override
    public Class<?> getTargetType() {
        return Currency.class;
    }
}

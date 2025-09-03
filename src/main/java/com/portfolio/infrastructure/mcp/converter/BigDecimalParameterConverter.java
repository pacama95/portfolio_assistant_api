package com.portfolio.infrastructure.mcp.converter;

import io.quarkiverse.mcp.server.ToolCallException;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Parameter converter strategy for BigDecimal types.
 * Handles conversion of monetary amounts, quantities, prices, and fees.
 */
@Singleton
public class BigDecimalParameterConverter implements ParameterConverter {

    // TODO: we could do it more extensible reading the parameters from application.properties
    private static final Set<String> SUPPORTED_PARAMETERS = Set.of(
            "quantity", "price", "fees", "fractionalMultiplier", 
            "currentPrice", "amount", "value", "cost"
    );

    @Override
    public Object convert(Object value, String parameterName) {
        switch (value) {
            case null -> {
                return null;
            }
            case BigDecimal bigDecimal -> {
                return bigDecimal;
            }
            case Number number -> {
                return BigDecimal.valueOf(number.doubleValue());
            }
            case String str -> {
                if (str.trim().isEmpty()) {
                    return null;
                }
                try {
                    return new BigDecimal(str.trim());
                } catch (NumberFormatException e) {
                    throw new ToolCallException("Invalid BigDecimal value for parameter '" + parameterName + "': " + str);
                }
            }
            default -> {
            }
        }

        throw new ToolCallException("Cannot convert " + value.getClass().getSimpleName() +
                " to BigDecimal for parameter '" + parameterName + "'");
    }

    @Override
    public boolean matches(String parameterName) {
        return SUPPORTED_PARAMETERS.contains(parameterName);
    }

    @Override
    public Class<?> getTargetType() {
        return BigDecimal.class;
    }
}

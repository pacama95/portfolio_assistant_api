package com.portfolio.infrastructure.mcp.converter;

import com.portfolio.domain.model.TransactionType;
import io.quarkiverse.mcp.server.ToolCallException;
import jakarta.inject.Singleton;

import java.util.Set;

/**
 * Parameter converter strategy for TransactionType enum types.
 * Handles conversion of transaction type parameters.
 */
@Singleton
public class TransactionTypeParameterConverter implements ParameterConverter {

    // TODO: we could do it more extensible reading the parameters from application.properties
    private static final Set<String> SUPPORTED_PARAMETERS = Set.of(
            "type", "transactionType", "operationType", "actionType"
    );

    @Override
    public Object convert(Object value, String parameterName) {
        switch (value) {
            case null -> {
                return null;
            }
            case TransactionType transactionType -> {
                return transactionType;
            }
            case String str -> {
                if (str.trim().isEmpty()) {
                    return null;
                }
                try {
                    return TransactionType.valueOf(str.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new ToolCallException("Invalid TransactionType value for parameter '" + parameterName +
                            "': " + str + ". Valid values: " + java.util.Arrays.toString(TransactionType.values()));
                }
            }
            default -> {
            }
        }

        throw new ToolCallException("Cannot convert " + value.getClass().getSimpleName() +
                " to TransactionType for parameter '" + parameterName + "'");
    }

    @Override
    public boolean matches(String parameterName) {
        return SUPPORTED_PARAMETERS.contains(parameterName);
    }

    @Override
    public Class<?> getTargetType() {
        return TransactionType.class;
    }
}

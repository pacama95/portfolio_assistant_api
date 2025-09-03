package com.portfolio.infrastructure.mcp.converter;

import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Service that converts Object parameters to their appropriate types using the Strategy pattern.
 * Automatically selects the appropriate converter based on parameter names.
 */
@Slf4j
@ApplicationScoped
public class ParameterConversionService {

    private final List<ParameterConverter> converters;

    @Inject
    public ParameterConversionService(Instance<ParameterConverter> converterInstances) {
        this.converters = converterInstances.stream().toList();
        
        // Log registered converters for debugging
        Log.infof("Registered %d parameter converters: %s", 
                converters.size(), 
                converters.stream()
                        .map(c -> c.getClass().getSimpleName() + "(" + c.getTargetType().getSimpleName() + ")")
                        .toList());
    }

    /**
     * Converts the input value to the appropriate type based on the parameter name.
     * 
     * @param value The input value to convert
     * @param parameterName The name of the parameter being converted
     * @return The converted value, or the original value if no converter matches
     * @throws ToolCallException if conversion fails
     */
    public Object convert(Object value, String parameterName) {
        if (value == null) {
            return null;
        }

        Optional<ParameterConverter> matchingConverter = findFirstMatchingConverter(parameterName);

        if (matchingConverter.isPresent()) {
            ParameterConverter converter = matchingConverter.get();

            log.debug("Converting parameter {} using {} (target type: {})",
                    parameterName,
                    converter.getClass().getSimpleName(),
                    converter.getTargetType().getSimpleName());
            
            return converter.convert(value, parameterName);
        }

        log.warn("No converter found for parameter {}, returning original value of type {}",
                parameterName, value.getClass().getSimpleName());
        
        return value;
    }

    private Optional<ParameterConverter> findFirstMatchingConverter(String parameterName) {
        return converters.stream()
                .filter(converter -> converter.matches(parameterName))
                .findFirst();
    }
}

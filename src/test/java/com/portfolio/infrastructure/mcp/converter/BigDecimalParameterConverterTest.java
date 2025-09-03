package com.portfolio.infrastructure.mcp.converter;

import io.quarkiverse.mcp.server.ToolCallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BigDecimalParameterConverter Tests")
class BigDecimalParameterConverterTest {

    private BigDecimalParameterConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BigDecimalParameterConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"quantity", "price", "fees", "fractionalMultiplier", "currentPrice", "amount", "value", "cost"})
    @DisplayName("Should match supported parameter names")
    void shouldMatchSupportedParameterNames(String parameterName) {
        assertTrue(converter.matches(parameterName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ticker", "type", "date", "currency", "notes", "other"})
    @DisplayName("Should not match unsupported parameter names")
    void shouldNotMatchUnsupportedParameterNames(String parameterName) {
        assertFalse(converter.matches(parameterName));
    }

    @Test
    @DisplayName("Should return BigDecimal as target type")
    void shouldReturnBigDecimalAsTargetType() {
        assertEquals(BigDecimal.class, converter.getTargetType());
    }

    @Test
    @DisplayName("Should return null for null input")
    void shouldReturnNullForNullInput() {
        Object result = converter.convert(null, "price");
        assertNull(result);
    }

    @Test
    @DisplayName("Should return existing BigDecimal unchanged")
    void shouldReturnExistingBigDecimalUnchanged() {
        BigDecimal input = new BigDecimal("123.45");
        Object result = converter.convert(input, "price");
        
        assertSame(input, result);
    }

    @Test
    @DisplayName("Should convert Number types to BigDecimal")
    void shouldConvertNumberTypesToBigDecimal() {
        Object result = converter.convert(123.45, "price");
        
        assertInstanceOf(BigDecimal.class, result);
        assertEquals(BigDecimal.valueOf(123.45), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"100.50", "0.00", "999999.9999", "1000"})
    @DisplayName("Should convert valid string values to BigDecimal")
    void shouldConvertValidStringValuesToBigDecimal(String input) {
        Object result = converter.convert(input, "price");
        
        assertInstanceOf(BigDecimal.class, result);
        assertEquals(new BigDecimal(input), result);
    }

    @Test
    @DisplayName("Should handle trimming of string values")
    void shouldHandleTrimmingOfStringValues() {
        Object result = converter.convert("  100.50  ", "price");
        
        assertInstanceOf(BigDecimal.class, result);
        assertEquals(new BigDecimal("100.50"), result);
    }

    @Test
    @DisplayName("Should return null for empty string")
    void shouldReturnNullForEmptyString() {
        Object result = converter.convert("", "price");
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null for whitespace-only string")
    void shouldReturnNullForWhitespaceOnlyString() {
        Object result = converter.convert("   ", "price");
        assertNull(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "abc123", "12.34.56"})
    @DisplayName("Should throw ToolCallException for invalid string values")
    void shouldThrowToolCallExceptionForInvalidStringValues(String input) {
        ToolCallException exception = assertThrows(ToolCallException.class, 
                () -> converter.convert(input, "price"));
        
        assertTrue(exception.getMessage().contains("Invalid BigDecimal value for parameter 'price'"));
        assertTrue(exception.getMessage().contains(input));
    }

    @Test
    @DisplayName("Should throw ToolCallException for unsupported types")
    void shouldThrowToolCallExceptionForUnsupportedTypes() {
        ToolCallException exception = assertThrows(ToolCallException.class, 
                () -> converter.convert(new Object(), "price"));
        
        assertTrue(exception.getMessage().contains("Cannot convert Object to BigDecimal for parameter 'price'"));
    }
}

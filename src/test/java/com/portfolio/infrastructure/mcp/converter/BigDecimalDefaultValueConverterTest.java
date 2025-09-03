package com.portfolio.infrastructure.mcp.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BigDecimalDefaultValueConverter Tests")
class BigDecimalDefaultValueConverterTest {

    private BigDecimalDefaultValueConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BigDecimalDefaultValueConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "100.50", "1000", "0.123456", "999999.9999"})
    @DisplayName("Should convert valid numeric strings to BigDecimal")
    void shouldConvertValidNumericStrings(String input) {
        // When
        BigDecimal result = converter.convert(input);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal(input), result);
    }

    @Test
    @DisplayName("Should return null for null input")
    void shouldReturnNullForNullInput() {
        // When
        BigDecimal result = converter.convert(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null for empty string")
    void shouldReturnNullForEmptyString() {
        // When
        BigDecimal result = converter.convert("");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null for whitespace-only string")
    void shouldReturnNullForWhitespaceOnlyString() {
        // When
        BigDecimal result = converter.convert("   ");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle trimming of input string")
    void shouldHandleTrimmingOfInputString() {
        // When
        BigDecimal result = converter.convert("  100.50  ");

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("100.50"), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12.34.56", "not-a-number", "1,000.00"})
    @DisplayName("Should throw IllegalArgumentException for invalid numeric strings")
    void shouldThrowExceptionForInvalidNumericStrings(String input) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert(input)
        );

        assertTrue(exception.getMessage().contains("Invalid default value for BigDecimal"));
        assertInstanceOf(NumberFormatException.class, exception.getCause());
    }
}

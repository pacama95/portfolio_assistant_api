package com.portfolio.infrastructure.mcp.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LocalDateDefaultValueConverter Tests")
class LocalDateDefaultValueConverterTest {

    private LocalDateDefaultValueConverter converter;

    @BeforeEach
    void setUp() {
        converter = new LocalDateDefaultValueConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-01-15", "2023-12-31", "2025-06-30", "2024-02-29"})
    @DisplayName("Should convert valid ISO date strings to LocalDate")
    void shouldConvertValidIsoDateStrings(String input) {
        // When
        LocalDate result = converter.convert(input);

        // Then
        assertNotNull(result);
        assertEquals(LocalDate.parse(input), result);
    }

    @Test
    @DisplayName("Should convert TODAY keyword to current date")
    void shouldConvertTodayKeywordToCurrentDate() {
        // When
        LocalDate result = converter.convert("TODAY");

        // Then
        assertNotNull(result);
        assertEquals(LocalDate.now(), result);
    }

    @Test
    @DisplayName("Should convert today keyword case insensitive")
    void shouldConvertTodayKeywordCaseInsensitive() {
        // When
        LocalDate resultLower = converter.convert("today");
        LocalDate resultUpper = converter.convert("TODAY");
        LocalDate resultMixed = converter.convert("Today");

        // Then
        assertNotNull(resultLower);
        assertNotNull(resultUpper);
        assertNotNull(resultMixed);
        assertEquals(LocalDate.now(), resultLower);
        assertEquals(LocalDate.now(), resultUpper);
        assertEquals(LocalDate.now(), resultMixed);
    }

    @Test
    @DisplayName("Should return null for null input")
    void shouldReturnNullForNullInput() {
        // When
        LocalDate result = converter.convert(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null for empty string")
    void shouldReturnNullForEmptyString() {
        // When
        LocalDate result = converter.convert("");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null for whitespace-only string")
    void shouldReturnNullForWhitespaceOnlyString() {
        // When
        LocalDate result = converter.convert("   ");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle trimming of input string")
    void shouldHandleTrimmingOfInputString() {
        // When
        LocalDate result = converter.convert("  2024-01-15  ");

        // Then
        assertNotNull(result);
        assertEquals(LocalDate.of(2024, 1, 15), result);
    }

    @Test
    @DisplayName("Should handle trimming of TODAY keyword")
    void shouldHandleTrimmingOfTodayKeyword() {
        // When
        LocalDate result = converter.convert("  TODAY  ");

        // Then
        assertNotNull(result);
        assertEquals(LocalDate.now(), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-13-01", "2024-01-32", "24-01-15", "2024/01/15", "invalid-date", "2024-1-1"})
    @DisplayName("Should throw IllegalArgumentException for invalid date strings")
    void shouldThrowExceptionForInvalidDateStrings(String input) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert(input)
        );

        assertTrue(exception.getMessage().contains("Invalid default value for LocalDate"));
        assertTrue(exception.getMessage().contains("Expected format: YYYY-MM-DD or 'TODAY'"));
    }
}

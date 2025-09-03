package com.portfolio.infrastructure.rest.mapper;

import com.portfolio.domain.model.Dividend;
import com.portfolio.infrastructure.rest.dto.DividendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DividendMapperTest {
    private DividendMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DividendMapper.class);
    }

    @Test
    void testToResponse_normalizesAmount() {
        // Given
        Dividend dividend = new Dividend(
            "AAPL",
            "XNAS", 
            "NASDAQ",
            LocalDate.of(2023, 11, 10),
            new BigDecimal("0.2400000000") // More precision than needed
        );

        // When
        DividendResponse response = mapper.toResponse(dividend);

        // Then
        assertNotNull(response);
        assertEquals("AAPL", response.symbol());
        assertEquals("XNAS", response.micCode());
        assertEquals("NASDAQ", response.exchange());
        assertEquals(LocalDate.of(2023, 11, 10), response.exDate());
        assertEquals(new BigDecimal("0.2400"), response.amount()); // Normalized to 4 decimal places
    }

    @Test
    void testToResponses_withMultipleDividends() {
        // Given
        Dividend dividend1 = new Dividend("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 5, 12), new BigDecimal("0.24"));
        Dividend dividend2 = new Dividend("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 8, 11), new BigDecimal("0.24"));
        List<Dividend> dividends = Arrays.asList(dividend1, dividend2);

        // When
        List<DividendResponse> responses = mapper.toResponses(dividends);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        
        DividendResponse response1 = responses.get(0);
        assertEquals("AAPL", response1.symbol());
        assertEquals(LocalDate.of(2023, 5, 12), response1.exDate());
        assertEquals(new BigDecimal("0.2400"), response1.amount());
        
        DividendResponse response2 = responses.get(1);
        assertEquals("AAPL", response2.symbol());
        assertEquals(LocalDate.of(2023, 8, 11), response2.exDate());
        assertEquals(new BigDecimal("0.2400"), response2.amount());
    }

    @Test
    void testToResponseMap_withMultipleTickers() {
        // Given
        Dividend aaplDividend1 = new Dividend("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 5, 12), new BigDecimal("0.24"));
        Dividend aaplDividend2 = new Dividend("AAPL", "XNAS", "NASDAQ", LocalDate.of(2023, 8, 11), new BigDecimal("0.24"));
        Dividend msftDividend = new Dividend("MSFT", "XNAS", "NASDAQ", LocalDate.of(2023, 6, 14), new BigDecimal("0.75"));
        
        Map<String, List<Dividend>> dividendsMap = Map.of(
            "AAPL", Arrays.asList(aaplDividend1, aaplDividend2),
            "MSFT", List.of(msftDividend)
        );

        // When
        Map<String, List<DividendResponse>> responseMap = mapper.toResponseMap(dividendsMap);

        // Then
        assertNotNull(responseMap);
        assertEquals(2, responseMap.size());
        
        assertTrue(responseMap.containsKey("AAPL"));
        assertTrue(responseMap.containsKey("MSFT"));
        
        List<DividendResponse> aaplResponses = responseMap.get("AAPL");
        assertEquals(2, aaplResponses.size());
        assertEquals("AAPL", aaplResponses.get(0).symbol());
        assertEquals(new BigDecimal("0.2400"), aaplResponses.get(0).amount());
        
        List<DividendResponse> msftResponses = responseMap.get("MSFT");
        assertEquals(1, msftResponses.size());
        assertEquals("MSFT", msftResponses.get(0).symbol());
        assertEquals(new BigDecimal("0.7500"), msftResponses.get(0).amount());
    }

    @ParameterizedTest
    @MethodSource("provideDividendAmountNormalizationTestCases")
    void testAmountNormalization(String testName, BigDecimal input, BigDecimal expected) {
        // Given
        Dividend dividend = new Dividend("TEST", "XNAS", "NASDAQ", LocalDate.of(2023, 1, 1), input);

        // When
        DividendResponse response = mapper.toResponse(dividend);

        // Then
        if (expected == null) {
            assertNull(response.amount(), testName + ": Amount should be null");
        } else {
            assertEquals(expected, response.amount(), testName + ": Amount normalization failed");
        }
    }

    static Stream<Arguments> provideDividendAmountNormalizationTestCases() {
        return Stream.of(
            Arguments.of("Standard amount", new BigDecimal("0.24"), new BigDecimal("0.2400")),
            Arguments.of("High precision amount", new BigDecimal("0.240000000"), new BigDecimal("0.2400")),
            Arguments.of("Low precision amount", new BigDecimal("0.2"), new BigDecimal("0.2000")),
            Arguments.of("Large amount", new BigDecimal("15.75"), new BigDecimal("15.7500")),
            Arguments.of("Zero amount", BigDecimal.ZERO, new BigDecimal("0.0000")),
            Arguments.of("Null amount", null, null)
        );
    }

    @Test
    void testFieldMapping_allFields() {
        // Given
        LocalDate testDate = LocalDate.of(2023, 12, 15);
        Dividend dividend = new Dividend(
            "GOOGL",
            "XNAS",
            "NASDAQ Global Select",
            testDate,
            new BigDecimal("1.234567")
        );

        // When
        DividendResponse response = mapper.toResponse(dividend);

        // Then
        assertNotNull(response);
        assertEquals("GOOGL", response.symbol());
        assertEquals("XNAS", response.micCode());
        assertEquals("NASDAQ Global Select", response.exchange());
        assertEquals(testDate, response.exDate());
        assertEquals(new BigDecimal("1.2346"), response.amount());
    }
}

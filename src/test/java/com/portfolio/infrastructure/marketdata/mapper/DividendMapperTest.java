package com.portfolio.infrastructure.marketdata.mapper;

import com.portfolio.domain.model.Dividend;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataDividendResponse;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataDividendsMeta;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataDividendsWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DividendMapperTest {
    private DividendMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DividendMapper.class);
    }

    @Test
    void testToDomainWithWrapper() {
        // Given
        TwelveDataDividendsMeta meta = createMeta("AAPL", "Apple Inc.", "USD", "NASDAQ", "XNGS", "America/New_York");
        
        TwelveDataDividendResponse dividend1 = createDividend(LocalDate.of(2023, 11, 10), new BigDecimal("0.24"));
        TwelveDataDividendResponse dividend2 = createDividend(LocalDate.of(2023, 8, 11), new BigDecimal("0.24"));
        
        TwelveDataDividendsWrapper wrapper = new TwelveDataDividendsWrapper();
        wrapper.setMeta(meta);
        wrapper.setDividends(Arrays.asList(dividend1, dividend2));

        // When
        List<Dividend> dividends = mapper.toDomain(wrapper);

        // Then
        assertNotNull(dividends);
        assertEquals(2, dividends.size());
        
        Dividend firstDividend = dividends.get(0);
        assertEquals("AAPL", firstDividend.getSymbol());
        assertEquals("XNGS", firstDividend.getMicCode());
        assertEquals("NASDAQ", firstDividend.getExchange());
        assertEquals(LocalDate.of(2023, 11, 10), firstDividend.getExDate());
        assertEquals(new BigDecimal("0.24"), firstDividend.getAmount());
        
        Dividend secondDividend = dividends.get(1);
        assertEquals("AAPL", secondDividend.getSymbol());
        assertEquals("XNGS", secondDividend.getMicCode());
        assertEquals("NASDAQ", secondDividend.getExchange());
        assertEquals(LocalDate.of(2023, 8, 11), secondDividend.getExDate());
        assertEquals(new BigDecimal("0.24"), secondDividend.getAmount());
    }

    @Test
    void testToDomainWithNullDividend() {
        // Given
        TwelveDataDividendsMeta meta = createMeta("AAPL", "Apple Inc.", "USD", "NASDAQ", "XNGS", "America/New_York");

        // When
        Dividend result = mapper.toDomain(null, meta);

        // Then
        assertNull(result);
    }

    @Test
    void testToDomainWithNullMeta() {
        // Given
        TwelveDataDividendResponse dividend = createDividend(LocalDate.of(2023, 11, 10), new BigDecimal("0.24"));

        // When
        Dividend result = mapper.toDomain(dividend, null);

        // Then
        assertNotNull(result);
        assertNull(result.getSymbol());
        assertNull(result.getMicCode());
        assertNull(result.getExchange());
        assertEquals(LocalDate.of(2023, 11, 10), result.getExDate());
        assertEquals(new BigDecimal("0.24"), result.getAmount());
    }

    private TwelveDataDividendsMeta createMeta(String symbol, String name, String currency, String exchange, String micCode, String timezone) {
        TwelveDataDividendsMeta meta = new TwelveDataDividendsMeta();
        meta.setSymbol(symbol);
        meta.setName(name);
        meta.setCurrency(currency);
        meta.setExchange(exchange);
        meta.setMicCode(micCode);
        meta.setExchangeTimezone(timezone);
        return meta;
    }

    private TwelveDataDividendResponse createDividend(LocalDate exDate, BigDecimal amount) {
        TwelveDataDividendResponse dividend = new TwelveDataDividendResponse();
        dividend.setExDate(exDate);
        dividend.setAmount(amount);
        return dividend;
    }
}
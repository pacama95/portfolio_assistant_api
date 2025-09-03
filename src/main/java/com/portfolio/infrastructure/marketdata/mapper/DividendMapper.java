package com.portfolio.infrastructure.marketdata.mapper;

import com.portfolio.domain.model.Dividend;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataDividendResponse;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataDividendsWrapper;
import com.portfolio.infrastructure.marketdata.dto.TwelveDataDividendsMeta;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for converting between dividend DTOs and domain models
 */
@Mapper(componentModel = "cdi")
public interface DividendMapper {
    
    /**
     * Maps TwelveData dividends wrapper to list of domain models
     * Combines metadata from the wrapper with individual dividend data
     * 
     * @param wrapper the TwelveData dividends wrapper containing meta and dividends
     * @return the list of domain Dividend models
     */
    default List<Dividend> toDomain(TwelveDataDividendsWrapper wrapper) {
        if (wrapper == null || wrapper.getDividends() == null) {
            return List.of();
        }
        
        TwelveDataDividendsMeta meta = wrapper.getMeta();
        return wrapper.getDividends().stream()
            .map(dividend -> new Dividend(
                meta != null ? meta.getSymbol() : null,
                meta != null ? meta.getMicCode() : null,
                meta != null ? meta.getExchange() : null,
                dividend.getExDate(),
                dividend.getAmount()
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Maps individual TwelveData dividend response with separate metadata to domain model
     * 
     * @param dividend the individual dividend data
     * @param meta the metadata containing symbol, mic_code, exchange info
     * @return the domain Dividend model
     */
    default Dividend toDomain(TwelveDataDividendResponse dividend, TwelveDataDividendsMeta meta) {
        if (dividend == null) {
            return null;
        }
        
        return new Dividend(
            meta != null ? meta.getSymbol() : null,
            meta != null ? meta.getMicCode() : null,
            meta != null ? meta.getExchange() : null,
            dividend.getExDate(),
            dividend.getAmount()
        );
    }
}

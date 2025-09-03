package com.portfolio.infrastructure.rest.mapper;

import com.portfolio.domain.model.Dividend;
import com.portfolio.infrastructure.rest.dto.DividendResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi")
public interface DividendMapper {
    int MONETARY_SCALE = 4;
    RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Mapping(target = "amount", expression = "java(normalizeMonetary(dividend.getAmount()))")
    DividendResponse toResponse(Dividend dividend);

    List<DividendResponse> toResponses(List<Dividend> dividends);

    /**
     * Maps a map of ticker to dividends list to a map of ticker to dividend responses list
     */
    default Map<String, List<DividendResponse>> toResponseMap(Map<String, List<Dividend>> dividendsMap) {
        if (dividendsMap == null) {
            return null;
        }
        return dividendsMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> toResponses(entry.getValue())
            ));
    }

    // Normalization helper
    default BigDecimal normalizeMonetary(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(MONETARY_SCALE, ROUNDING);
    }
}

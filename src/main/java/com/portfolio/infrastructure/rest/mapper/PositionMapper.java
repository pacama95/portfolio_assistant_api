package com.portfolio.infrastructure.rest.mapper;

import com.portfolio.domain.model.Position;
import com.portfolio.domain.model.CurrentPosition;
import com.portfolio.infrastructure.rest.dto.PositionResponse;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper(componentModel = "cdi")
public interface PositionMapper {
    int MONETARY_SCALE = 4;
    int QUANTITY_SCALE = 6;
    RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Mapping(target = "totalQuantity", expression = "java(normalizeQuantity(position.getTotalQuantity()))")
    @Mapping(target = "averagePrice", expression = "java(normalizeMonetary(position.getAveragePrice()))")
    @Mapping(target = "currentPrice", expression = "java(normalizeMonetary(position.getCurrentPrice()))")
    @Mapping(target = "totalCost", expression = "java(normalizeMonetary(position.getTotalCost()))")
    @Mapping(target = "marketValue", expression = "java(normalizeMonetary(position.getMarketValue()))")
    @Mapping(target = "unrealizedGainLoss", expression = "java(normalizeMonetary(position.getUnrealizedGainLoss()))")
    @Mapping(target = "unrealizedGainLossPercentage", expression = "java(normalizeMonetary(position.getUnrealizedGainLossPercentage()))")
    PositionResponse toResponse(Position position);

    List<PositionResponse> toResponses(List<Position> positions);

    /**
     * Maps CurrentPosition to PositionResponse with real-time current price
     */
    @Mapping(target = "totalQuantity", expression = "java(normalizeQuantity(currentPosition.getTotalQuantity()))")
    @Mapping(target = "averagePrice", expression = "java(normalizeMonetary(currentPosition.getAveragePrice()))")
    @Mapping(target = "currentPrice", expression = "java(normalizeMonetary(currentPosition.getCurrentPrice()))")
    @Mapping(target = "totalCost", expression = "java(normalizeMonetary(currentPosition.getTotalCost()))")
    @Mapping(target = "marketValue", expression = "java(normalizeMonetary(currentPosition.getMarketValue()))")
    @Mapping(target = "unrealizedGainLoss", expression = "java(normalizeMonetary(currentPosition.getUnrealizedGainLoss()))")
    @Mapping(target = "unrealizedGainLossPercentage", expression = "java(normalizeMonetary(currentPosition.getUnrealizedGainLossPercentage()))")
    PositionResponse toResponse(CurrentPosition currentPosition);

    List<PositionResponse> toCurrentPositionResponses(List<CurrentPosition> currentPositions);

    // Normalization helpers
    default BigDecimal normalizeMonetary(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(MONETARY_SCALE, ROUNDING);
    }
    default BigDecimal normalizeQuantity(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(QUANTITY_SCALE, ROUNDING);
    }
} 
package com.portfolio.infrastructure.rest.mapper;

import com.portfolio.application.command.CreateTransactionCommand;
import com.portfolio.application.command.UpdateTransactionCommand;
import com.portfolio.domain.model.Transaction;
import com.portfolio.infrastructure.rest.dto.CreateTransactionRequest;
import com.portfolio.infrastructure.rest.dto.TransactionResponse;
import com.portfolio.infrastructure.rest.dto.UpdateTransactionRequest;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Mapper(componentModel = "cdi")
public interface TransactionMapper {
    int MONETARY_SCALE = 4;
    int QUANTITY_SCALE = 6;
    RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Mapping(target = "quantity", expression = "java(normalizeQuantity(transaction.getQuantity()))")
    @Mapping(target = "price", expression = "java(normalizeMonetary(transaction.getPrice()))")
    @Mapping(target = "fees", expression = "java(normalizeMonetary(transaction.getFees()))")
    @Mapping(target = "totalValue", expression = "java(normalizeMonetary(transaction.getTotalValue()))")
    @Mapping(target = "totalCost", expression = "java(normalizeMonetary(transaction.getTotalCost()))")
    @Mapping(target = "fractionalMultiplier", expression = "java(normalizeMonetary(transaction.getFractionalMultiplier()))")
    TransactionResponse toResponse(Transaction transaction);

    @Mapping(target = "quantity", expression = "java(normalizeQuantity(createTransactionRequest.quantity()))")
    @Mapping(target = "price", expression = "java(normalizeMonetary(createTransactionRequest.price()))")
    @Mapping(target = "fees", expression = "java(normalizeMonetary(createTransactionRequest.fees()))")
    @Mapping(target = "fractionalMultiplier", expression = "java(normalizeMonetary(createTransactionRequest.fractionalMultiplier()))")
    CreateTransactionCommand toCreateTransactionCommand(CreateTransactionRequest createTransactionRequest);

    @Mapping(target = "quantity", expression = "java(normalizeQuantity(updateTransactionRequest.quantity()))")
    @Mapping(target = "price", expression = "java(normalizeMonetary(updateTransactionRequest.price()))")
    @Mapping(target = "fees", expression = "java(normalizeMonetary(updateTransactionRequest.fees()))")
    @Mapping(target = "fractionalMultiplier", expression = "java(normalizeMonetary(updateTransactionRequest.fractionalMultiplier()))")
    UpdateTransactionCommand toUpdateTransactionCommand(UUID transactionId, UpdateTransactionRequest updateTransactionRequest);

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
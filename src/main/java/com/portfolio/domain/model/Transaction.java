package com.portfolio.domain.model;

import com.portfolio.domain.event.DomainEvent;
import com.portfolio.domain.event.TransactionCreatedEvent;
import com.portfolio.domain.event.TransactionUpdatedEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor(onConstructor = @__({@Default}))
public class Transaction {
    private final UUID id;
    private String ticker;
    private TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal fees;
    private Currency currency;
    private LocalDate transactionDate;
    private String notes;
    private Boolean isActive;
    private Boolean isFractional;
    private BigDecimal fractionalMultiplier;
    private Currency commissionCurrency;
    private List<DomainEvent<?>> domainEvents = new ArrayList<>();

    public Transaction(String ticker,
                       TransactionType transactionType,
                       BigDecimal quantity,
                       BigDecimal price,
                       BigDecimal fees,
                       Currency currency,
                       LocalDate transactionDate,
                       String notes,
                       Boolean isActive,
                       Boolean isFractional,
                       BigDecimal fractionalMultiplier,
                       Currency commissionCurrency) {
        this.id = null;
        this.ticker = ticker;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.price = price;
        this.fees = fees;
        this.currency = currency;
        this.transactionDate = transactionDate;
        this.notes = notes;
        this.isActive = isActive;
        this.isFractional = isFractional;
        this.fractionalMultiplier = fractionalMultiplier;
        this.commissionCurrency = commissionCurrency;
        this.domainEvents.add(new TransactionCreatedEvent(this));
    }

    public BigDecimal getTotalValue() {
        return quantity.multiply(price);
    }

    public BigDecimal getTotalCost() {
        return getTotalValue().add(fees != null ? fees : BigDecimal.ZERO);
    }

    public void update(String ticker,
                       TransactionType transactionType,
                       BigDecimal quantity,
                       BigDecimal price,
                       BigDecimal fees,
                       Currency currency,
                       LocalDate transactionDate,
                       String notes,
                       Boolean isFractional,
                       BigDecimal fractionalMultiplier,
                       Currency commissionCurrency) {

        if (ticker != null) this.ticker = ticker;
        if (transactionType != null) this.transactionType = transactionType;
        if (quantity != null) this.quantity = quantity;
        if (price != null) this.price = price;
        if (fees != null) this.fees = fees;
        if (currency != null) this.currency = currency;
        if (transactionDate != null) this.transactionDate = transactionDate;
        if (notes != null) this.notes = notes;
        if (isFractional != null) this.isFractional = isFractional;
        if (fractionalMultiplier != null) this.fractionalMultiplier = fractionalMultiplier;
        if (commissionCurrency != null) this.commissionCurrency = commissionCurrency;

        this.domainEvents.add(new TransactionUpdatedEvent(this));
    }
    
    public List<DomainEvent<?>> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public List<DomainEvent<?>> popEvents() {
        List<DomainEvent<?>> events = getDomainEvents();
        domainEvents.clear();

        return events;
    }
} 
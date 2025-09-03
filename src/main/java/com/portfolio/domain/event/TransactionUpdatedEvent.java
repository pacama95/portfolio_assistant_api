package com.portfolio.domain.event;

import com.portfolio.domain.model.Transaction;
public class TransactionUpdatedEvent extends DomainEvent<Transaction> {
    public TransactionUpdatedEvent(Transaction transaction) {
        super(transaction);
    }
}

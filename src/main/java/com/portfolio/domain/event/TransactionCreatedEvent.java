package com.portfolio.domain.event;

import com.portfolio.domain.model.Transaction;

public class TransactionCreatedEvent extends DomainEvent<Transaction> {

    public TransactionCreatedEvent(Transaction transaction) {
        super(transaction);
    }
}

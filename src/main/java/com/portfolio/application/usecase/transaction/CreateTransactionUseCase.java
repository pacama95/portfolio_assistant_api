package com.portfolio.application.usecase.transaction;

import com.portfolio.application.command.CreateTransactionCommand;
import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.port.TransactionRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CreateTransactionUseCase {

    @Inject
    TransactionRepository transactionRepository;

    @WithTransaction
    public Uni<Transaction> execute(CreateTransactionCommand command) {
        Transaction transaction = new Transaction(
                command.ticker(),
                command.transactionType(),
                command.quantity(),
                command.price(),
                command.fees(),
                command.currency(),
                command.transactionDate(),
                command.notes(),
                true,
                command.isFractional(),
                command.fractionalMultiplier(),
                command.commissionCurrency()
        );

        return transactionRepository.save(transaction)
                .invoke(Transaction::popEvents) // TODO: Popping events, in the future we should publish this to a queue
                .onFailure().transform(throwable -> new ServiceException(Errors.CreateTransaction.PERSISTENCE_ERROR, throwable))
                .onItem().invoke(saved -> Log.info("Transaction saved for ticker %s".formatted(saved.getTicker())));
    }
} 
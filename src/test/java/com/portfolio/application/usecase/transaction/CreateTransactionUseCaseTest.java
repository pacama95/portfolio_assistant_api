package com.portfolio.application.usecase.transaction;

import com.portfolio.application.command.CreateTransactionCommand;
import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.domain.port.TransactionRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateTransactionUseCaseTest {
    private TransactionRepository transactionRepository;
    private CreateTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        useCase = new CreateTransactionUseCase();
        useCase.transactionRepository = transactionRepository;
    }

    @Test
    void testExecuteSuccess() {
        // Given
        CreateTransactionCommand command = createValidCommand();
        UUID transactionId = UUID.randomUUID();
        Transaction savedTransaction = new Transaction(
            transactionId,
            command.ticker(),
            command.transactionType(),
            command.quantity(),
            command.price(),
            command.fees(),
            command.currency(),
            command.transactionDate(),
            command.notes(),
            true, // isActive
            command.isFractional(),
            command.fractionalMultiplier(),
            command.commissionCurrency(),
            Collections.emptyList()
        );

        when(transactionRepository.save(any(Transaction.class)))
            .thenReturn(Uni.createFrom().item(savedTransaction));

        // When
        Uni<Transaction> result = useCase.execute(command);

        // Then
        Transaction actualTransaction = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertNotNull(actualTransaction);
        assertEquals(savedTransaction.getId(), actualTransaction.getId());
        assertEquals(savedTransaction.getTicker(), actualTransaction.getTicker());
        assertEquals(savedTransaction.getTransactionType(), actualTransaction.getTransactionType());
        assertEquals(savedTransaction.getQuantity(), actualTransaction.getQuantity());
        assertEquals(savedTransaction.getPrice(), actualTransaction.getPrice());
        assertEquals(savedTransaction.getCurrency(), actualTransaction.getCurrency());
        assertEquals(savedTransaction.getTransactionDate(), actualTransaction.getTransactionDate());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testExecuteRepositoryFailure() {
        // Given
        CreateTransactionCommand command = createValidCommand();
        RuntimeException exception = new RuntimeException("Database error");

        when(transactionRepository.save(any(Transaction.class)))
            .thenReturn(Uni.createFrom().failure(exception));

        // When
        Uni<Transaction> result = useCase.execute(command);

        // Then
        var failure = result.subscribe()
                        .withSubscriber(UniAssertSubscriber.create())
                        .assertFailedWith(ServiceException.class)
                        .getFailure();

        assertEquals(Errors.CreateTransaction.PERSISTENCE_ERROR, ((ServiceException) failure).getError());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testExecuteWithComplexTransaction() {
        // Given
        CreateTransactionCommand command = createComplexCommand();
        UUID transactionId = UUID.randomUUID();
        Transaction savedTransaction = new Transaction(
            transactionId,
            command.ticker(),
            command.transactionType(),
            command.quantity(),
            command.price(),
            command.fees(),
            command.currency(),
            command.transactionDate(),
            command.notes(),
            true, // isActive
            command.isFractional(),
            command.fractionalMultiplier(),
            command.commissionCurrency(),
            Collections.emptyList()
        );

        when(transactionRepository.save(any(Transaction.class)))
            .thenReturn(Uni.createFrom().item(savedTransaction));

        // When
        Uni<Transaction> result = useCase.execute(command);

        // Then
        Transaction actualTransaction = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertNotNull(actualTransaction);
        assertEquals(savedTransaction.getId(), actualTransaction.getId());
        assertEquals(savedTransaction.getTicker(), actualTransaction.getTicker());
        assertEquals(savedTransaction.getFees(), actualTransaction.getFees());
        assertEquals(savedTransaction.getNotes(), actualTransaction.getNotes());
        assertEquals(savedTransaction.getIsFractional(), actualTransaction.getIsFractional());
        assertEquals(savedTransaction.getFractionalMultiplier(), actualTransaction.getFractionalMultiplier());
        assertEquals(savedTransaction.getCommissionCurrency(), actualTransaction.getCommissionCurrency());

        verify(transactionRepository).save(any(Transaction.class));
    }

    private CreateTransactionCommand createValidCommand() {
        return new CreateTransactionCommand(
            "AAPL",
            TransactionType.BUY,
            new BigDecimal("10"),
            new BigDecimal("150.50"),
            BigDecimal.ZERO,
            Currency.USD,
            LocalDate.of(2024, 1, 15),
            null,
            false,
            BigDecimal.ONE,
            null
        );
    }

    private CreateTransactionCommand createComplexCommand() {
        return new CreateTransactionCommand(
            "MSFT",
            TransactionType.SELL,
            new BigDecimal("5.5"),
            new BigDecimal("420.75"),
            new BigDecimal("9.99"),
            Currency.USD,
            LocalDate.of(2024, 2, 20),
            "Complex sell transaction",
            true,
            new BigDecimal("0.5"),
            Currency.EUR
        );
    }
}

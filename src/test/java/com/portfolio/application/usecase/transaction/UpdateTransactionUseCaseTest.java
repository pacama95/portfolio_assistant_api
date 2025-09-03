package com.portfolio.application.usecase.transaction;

import com.portfolio.application.command.UpdateTransactionCommand;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateTransactionUseCaseTest {
    private TransactionRepository transactionRepository;
    private UpdateTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        useCase = new UpdateTransactionUseCase();
        useCase.transactionRepository = transactionRepository;
    }

    @Test
    void testExecuteSuccess() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction existingTransaction = createExistingTransaction(transactionId);
        UpdateTransactionCommand command = createUpdateCommand(transactionId);
        Transaction updatedTransaction = createUpdatedTransaction(transactionId);

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().item(existingTransaction));
        when(transactionRepository.update(any(Transaction.class)))
            .thenReturn(Uni.createFrom().item(updatedTransaction));

        // When
        Uni<Transaction> result = useCase.execute(command);

        // Then
        Transaction actualTransaction = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertNotNull(actualTransaction);
        assertEquals(command.ticker(), actualTransaction.getTicker());
        assertEquals(command.transactionType(), actualTransaction.getTransactionType());
        assertEquals(command.quantity(), actualTransaction.getQuantity());
        assertEquals(command.price(), actualTransaction.getPrice());
        assertEquals(command.currency(), actualTransaction.getCurrency());
        assertEquals(command.transactionDate(), actualTransaction.getTransactionDate());
        assertEquals(command.notes(), actualTransaction.getNotes());
        assertEquals(command.isFractional(), actualTransaction.getIsFractional());
        assertEquals(command.fractionalMultiplier(), actualTransaction.getFractionalMultiplier());
        assertEquals(command.commissionCurrency(), actualTransaction.getCommissionCurrency());

        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).update(any(Transaction.class));
    }

    @Test
    void testExecuteTransactionNotFound() {
        // Given
        UUID transactionId = UUID.randomUUID();
        UpdateTransactionCommand command = createUpdateCommand(transactionId);

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().nullItem());

        // When
        Uni<Transaction> result = useCase.execute(command);

        // Then
        ServiceException thrown = (ServiceException) result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertFailedWith(ServiceException.class)
            .getFailure();

        assertEquals(Errors.UpdateTransaction.NOT_FOUND, thrown.getError());
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository, never()).update(any());
    }

    @Test
    void testExecutePartialUpdate() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction existingTransaction = createExistingTransaction(transactionId);
        UpdateTransactionCommand partialCommand = new UpdateTransactionCommand(
            transactionId,
            "GOOGL", // Only update ticker
            null, null, null, null, null, null, null, null, null, null
        );

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().item(existingTransaction));
        when(transactionRepository.update(any(Transaction.class)))
            .thenAnswer(invocation -> {
                Transaction updated = invocation.getArgument(0);
                return Uni.createFrom().item(updated);
            });

        // When
        Uni<Transaction> result = useCase.execute(partialCommand);

        // Then
        Transaction actualTransaction = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals("GOOGL", actualTransaction.getTicker());
        // Other fields should remain unchanged
        assertEquals(existingTransaction.getTransactionType(), actualTransaction.getTransactionType());
        assertEquals(existingTransaction.getQuantity(), actualTransaction.getQuantity());
        assertEquals(existingTransaction.getPrice(), actualTransaction.getPrice());
    }

    @ParameterizedTest
    @MethodSource("updateFieldProvider")
    void testExecuteUpdateSpecificFields(String field, Object value, String expectedValue) {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction existingTransaction = createExistingTransaction(transactionId);
        UpdateTransactionCommand command = createCommandForField(transactionId, field, value);

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().item(existingTransaction));
        when(transactionRepository.update(any(Transaction.class)))
            .thenAnswer(invocation -> {
                Transaction updated = invocation.getArgument(0);
                return Uni.createFrom().item(updated);
            });

        // When
        Uni<Transaction> result = useCase.execute(command);

        // Then
        Transaction actualTransaction = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertNotNull(actualTransaction);
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).update(any(Transaction.class));
    }

    @Test
    void testExecuteRepositoryFailure() {
        // Given
        UUID transactionId = UUID.randomUUID();
        UpdateTransactionCommand command = createUpdateCommand(transactionId);
        RuntimeException exception = new RuntimeException("Database error");

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().failure(exception));

        // When
        Uni<Transaction> result = useCase.execute(command);

        // Then
        result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertFailedWith(RuntimeException.class);

        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository, never()).update(any());
    }

    @Test
    void testExecuteUpdateFailure() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction existingTransaction = createExistingTransaction(transactionId);
        UpdateTransactionCommand command = createUpdateCommand(transactionId);
        RuntimeException exception = new RuntimeException("Update failed");

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().item(existingTransaction));
        when(transactionRepository.update(any(Transaction.class)))
            .thenReturn(Uni.createFrom().failure(exception));

        // When
        Uni<Transaction> result = useCase.execute(command);

        // Then
        result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertFailedWith(RuntimeException.class);

        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).update(any(Transaction.class));
    }

    @Test
    void testExecuteWithEmptyStrings() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction existingTransaction = createExistingTransaction(transactionId);
        UpdateTransactionCommand command = new UpdateTransactionCommand(
            transactionId,
            "", // Empty string - should not update
            TransactionType.SELL,
            new BigDecimal("20"),
            new BigDecimal("200.00"),
            new BigDecimal("5.00"),
            Currency.EUR,
            LocalDate.of(2024, 3, 1),
            "", // Empty string - should not update
            true,
            new BigDecimal("2.0"),
            Currency.GBP
        );

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().item(existingTransaction));
        when(transactionRepository.update(any(Transaction.class)))
            .thenAnswer(invocation -> {
                Transaction updated = invocation.getArgument(0);
                return Uni.createFrom().item(updated);
            });

        // When
        Uni<Transaction> result = useCase.execute(command);

        // Then
        Transaction actualTransaction = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Empty strings should not update the fields
        assertEquals(existingTransaction.getTicker(), actualTransaction.getTicker());
        assertEquals(existingTransaction.getNotes(), actualTransaction.getNotes());
        // Other fields should be updated
        assertEquals(command.transactionType(), actualTransaction.getTransactionType());
        assertEquals(command.quantity(), actualTransaction.getQuantity());
    }

    static Stream<Arguments> updateFieldProvider() {
        return Stream.of(
            Arguments.of("ticker", "NVDA", "NVDA"),
            Arguments.of("notes", "Updated notes", "Updated notes"),
            Arguments.of("quantity", new BigDecimal("25"), "25"),
            Arguments.of("price", new BigDecimal("300.00"), "300.00")
        );
    }

    private UpdateTransactionCommand createCommandForField(UUID transactionId, String field, Object value) {
        return switch (field) {
            case "ticker" -> new UpdateTransactionCommand(transactionId, (String) value, null, null, null, null, null, null, null, null, null, null);
            case "notes" -> new UpdateTransactionCommand(transactionId, null, null, null, null, null, null, null, (String) value, null, null, null);
            case "quantity" -> new UpdateTransactionCommand(transactionId, null, null, (BigDecimal) value, null, null, null, null, null, null, null, null);
            case "price" -> new UpdateTransactionCommand(transactionId, null, null, null, (BigDecimal) value, null, null, null, null, null, null, null);
            default -> throw new IllegalArgumentException("Unknown field: " + field);
        };
    }

    private Transaction createExistingTransaction(UUID id) {
        return new Transaction(
            id,
            "AAPL",
            TransactionType.BUY,
            new BigDecimal("10"),
            new BigDecimal("150.50"),
            new BigDecimal("9.99"),
            Currency.USD,
            LocalDate.of(2024, 1, 15),
            "Original notes",
            true,
            false,
            BigDecimal.ONE,
            Currency.USD,
            new ArrayList<>()
        );
    }

    private UpdateTransactionCommand createUpdateCommand(UUID transactionId) {
        return new UpdateTransactionCommand(
            transactionId,
            "MSFT",
            TransactionType.SELL,
            new BigDecimal("5"),
            new BigDecimal("420.75"),
            new BigDecimal("12.50"),
            Currency.EUR,
            LocalDate.of(2024, 2, 20),
            "Updated transaction notes",
            true,
            new BigDecimal("0.5"),
            Currency.GBP
        );
    }

    private Transaction createUpdatedTransaction(UUID id) {
        return new Transaction(
            id,
            "MSFT",
            TransactionType.SELL,
            new BigDecimal("5"),
            new BigDecimal("420.75"),
            new BigDecimal("12.50"),
            Currency.EUR,
            LocalDate.of(2024, 2, 20),
            "Updated transaction notes",
            true,
            true,
            new BigDecimal("0.5"),
            Currency.GBP,
            Collections.emptyList()
        );
    }
}

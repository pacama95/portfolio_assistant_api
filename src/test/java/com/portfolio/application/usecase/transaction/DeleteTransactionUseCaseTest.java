package com.portfolio.application.usecase.transaction;

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
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteTransactionUseCaseTest {
    private TransactionRepository transactionRepository;
    private DeleteTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        useCase = new DeleteTransactionUseCase();
        useCase.transactionRepository = transactionRepository;
    }

    @Test
    void testExecuteSuccess() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction existingTransaction = createTransaction(transactionId);

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().item(existingTransaction));
        when(transactionRepository.deleteById(transactionId))
            .thenReturn(Uni.createFrom().item(true));

        // When
        Uni<Boolean> result = useCase.execute(transactionId);

        // Then
        Boolean deleted = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertTrue(deleted);
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).deleteById(transactionId);
    }

    @Test
    void testExecuteTransactionNotFound() {
        // Given
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().nullItem());

        // When
        Uni<Boolean> result = useCase.execute(transactionId);

        // Then
        Boolean deleted = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertFalse(deleted);
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository, never()).deleteById(any());
    }

    @Test
    void testExecuteDeleteFailed() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction existingTransaction = createTransaction(transactionId);

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().item(existingTransaction));
        when(transactionRepository.deleteById(transactionId))
            .thenReturn(Uni.createFrom().item(false));

        // When
        Uni<Boolean> result = useCase.execute(transactionId);

        // Then
        Boolean deleted = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertFalse(deleted);
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).deleteById(transactionId);
    }

    @Test
    void testExecuteFindByIdFailure() {
        // Given
        UUID transactionId = UUID.randomUUID();
        RuntimeException exception = new RuntimeException("Database error");

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().failure(exception));

        // When
        Uni<Boolean> result = useCase.execute(transactionId);

        // Then
        var failure = result.subscribe()
                        .withSubscriber(UniAssertSubscriber.create())
                        .assertFailedWith(ServiceException.class)
                        .getFailure();

        assertEquals(Errors.DeleteTransaction.PERSISTENCE_ERROR, ((ServiceException) failure).getError());

        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository, never()).deleteById(any());
    }

    @Test
    void testExecuteDeleteByIdFailure() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction existingTransaction = createTransaction(transactionId);
        RuntimeException exception = new RuntimeException("Delete failed");

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().item(existingTransaction));
        when(transactionRepository.deleteById(transactionId))
            .thenReturn(Uni.createFrom().failure(exception));

        // When
        Uni<Boolean> result = useCase.execute(transactionId);

        // Then
        result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertFailedWith(RuntimeException.class);

        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).deleteById(transactionId);
    }

    @ParameterizedTest
    @MethodSource("transactionTypeProvider")
    void testExecuteWithDifferentTransactionTypes(TransactionType transactionType) {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = createTransactionWithType(transactionId, transactionType);

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().item(transaction));
        when(transactionRepository.deleteById(transactionId))
            .thenReturn(Uni.createFrom().item(true));

        // When
        Uni<Boolean> result = useCase.execute(transactionId);

        // Then
        Boolean deleted = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertTrue(deleted);
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).deleteById(transactionId);
    }

    static Stream<Arguments> transactionTypeProvider() {
        return Stream.of(
            Arguments.of(TransactionType.BUY),
            Arguments.of(TransactionType.SELL),
            Arguments.of(TransactionType.DIVIDEND)
        );
    }

    private Transaction createTransaction(UUID id) {
        return new Transaction(
            id,
            "AAPL",
            TransactionType.BUY,
            new BigDecimal("10"),
            new BigDecimal("150.50"),
            new BigDecimal("9.99"),
            Currency.USD,
            LocalDate.of(2024, 1, 15),
            "Test transaction",
            true,
            false,
            BigDecimal.ONE,
            null,
            Collections.emptyList()
        );
    }

    private Transaction createTransactionWithType(UUID id, TransactionType type) {
        return new Transaction(
            id,
            "TSLA",
            type,
            new BigDecimal("5"),
            new BigDecimal("250.00"),
            BigDecimal.ZERO,
            Currency.USD,
            LocalDate.of(2024, 2, 1),
            null,
            true,
            false,
            BigDecimal.ONE,
            null,
            Collections.emptyList()
        );
    }
}

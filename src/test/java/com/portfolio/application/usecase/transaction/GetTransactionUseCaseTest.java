package com.portfolio.application.usecase.transaction;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.domain.port.TransactionRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetTransactionUseCaseTest {
    private TransactionRepository transactionRepository;
    private GetTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        useCase = new GetTransactionUseCase();
        useCase.transactionRepository = transactionRepository;
    }

    @Test
    void testGetByIdSuccess() {
        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = createTransaction(transactionId, "AAPL");

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().item(transaction));

        // When
        Uni<Transaction> result = useCase.getById(transactionId);

        // Then
        Transaction actualTransaction = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(transaction, actualTransaction);
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void testGetByIdNotFound() {
        // Given
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().nullItem());

        // When
        Uni<Transaction> result = useCase.getById(transactionId);

        // Then
        Transaction actualTransaction = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertNull(actualTransaction);
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void testGetByIdFailure() {
        // Given
        UUID transactionId = UUID.randomUUID();
        RuntimeException exception = new RuntimeException("Database error");

        when(transactionRepository.findById(transactionId))
            .thenReturn(Uni.createFrom().failure(exception));

        // When
        Uni<Transaction> result = useCase.getById(transactionId);

        // Then
        result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertFailedWith(RuntimeException.class);

        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void testGetByTickerSuccess() {
        // Given
        String ticker = "AAPL";
        List<Transaction> transactions = Arrays.asList(
            createTransaction(UUID.randomUUID(), ticker),
            createTransaction(UUID.randomUUID(), ticker)
        );

        when(transactionRepository.findByTicker(ticker))
            .thenReturn(Uni.createFrom().item(transactions));

        // When
        Multi<Transaction> result = useCase.getByTicker(ticker);

        // Then
        List<Transaction> actualTransactions = result.collect().asList()
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(2, actualTransactions.size());
        assertEquals(transactions, actualTransactions);
        verify(transactionRepository).findByTicker(ticker);
    }

    @Test
    void testGetByTickerEmpty() {
        // Given
        String ticker = "NONEXISTENT";

        when(transactionRepository.findByTicker(ticker))
            .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        // When
        Multi<Transaction> result = useCase.getByTicker(ticker);

        // Then
        List<Transaction> actualTransactions = result.collect().asList()
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertTrue(actualTransactions.isEmpty());
        verify(transactionRepository).findByTicker(ticker);
    }

    @Test
    void testGetByTickerFailure() {
        // Given
        String ticker = "AAPL";
        RuntimeException exception = new RuntimeException("Database error");

        when(transactionRepository.findByTicker(ticker))
            .thenReturn(Uni.createFrom().failure(exception));

        // When
        Multi<Transaction> result = useCase.getByTicker(ticker);

        // Then
        result.collect().asList()
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertFailedWith(RuntimeException.class);

        verify(transactionRepository).findByTicker(ticker);
    }

    @Test
    void testGetAllSuccess() {
        // Given
        List<Transaction> allTransactions = Arrays.asList(
            createActiveTransaction(UUID.randomUUID(), "AAPL"),
            createInactiveTransaction(UUID.randomUUID(), "MSFT")
        );

        when(transactionRepository.findAll())
            .thenReturn(Uni.createFrom().item(allTransactions));

        // When
        Multi<Transaction> result = useCase.getAll();

        // Then
        List<Transaction> actualTransactions = result.collect().asList()
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(2, actualTransactions.size());
        assertEquals(allTransactions, actualTransactions);
        verify(transactionRepository).findAll();
    }

    @Test
    void testSearchTransactionsSuccess() {
        // Given
        String ticker = "AAPL";
        TransactionType type = TransactionType.BUY;
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 12, 31);
        List<Transaction> searchResults = List.of(
                createTransaction(UUID.randomUUID(), ticker)
        );

        when(transactionRepository.searchTransactions(ticker, type, fromDate, toDate))
            .thenReturn(Uni.createFrom().item(searchResults));

        // When
        Multi<Transaction> result = useCase.searchTransactions(ticker, type, fromDate, toDate);

        // Then
        List<Transaction> actualTransactions = result.collect().asList()
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(1, actualTransactions.size());
        assertEquals(searchResults, actualTransactions);
        verify(transactionRepository).searchTransactions(ticker, type, fromDate, toDate);
    }

    @Test
    void testSearchTransactionsWithNullParameters() {
        // Given
        List<Transaction> searchResults = Arrays.asList(
            createTransaction(UUID.randomUUID(), "AAPL"),
            createTransaction(UUID.randomUUID(), "MSFT")
        );

        when(transactionRepository.searchTransactions(null, null, null, null))
            .thenReturn(Uni.createFrom().item(searchResults));

        // When
        Multi<Transaction> result = useCase.searchTransactions(null, null, null, null);

        // Then
        List<Transaction> actualTransactions = result.collect().asList()
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(2, actualTransactions.size());
        assertEquals(searchResults, actualTransactions);
        verify(transactionRepository).searchTransactions(null, null, null, null);
    }

    @Test
    void testExistsSuccess() {
        // Given
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.existsById(transactionId))
            .thenReturn(Uni.createFrom().item(true));

        // When
        Uni<Boolean> result = useCase.exists(transactionId);

        // Then
        Boolean exists = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertTrue(exists);
        verify(transactionRepository).existsById(transactionId);
    }

    @Test
    void testExistsNotFound() {
        // Given
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.existsById(transactionId))
            .thenReturn(Uni.createFrom().item(false));

        // When
        Uni<Boolean> result = useCase.exists(transactionId);

        // Then
        Boolean exists = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertFalse(exists);
        verify(transactionRepository).existsById(transactionId);
    }

    @Test
    void testCountAllSuccess() {
        // Given
        Long expectedCount = 25L;

        when(transactionRepository.countAll())
            .thenReturn(Uni.createFrom().item(expectedCount));

        // When
        Uni<Long> result = useCase.countAll();

        // Then
        Long actualCount = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(expectedCount, actualCount);
        verify(transactionRepository).countAll();
    }

    @Test
    void testCountAllZero() {
        // Given
        when(transactionRepository.countAll())
            .thenReturn(Uni.createFrom().item(0L));

        // When
        Uni<Long> result = useCase.countAll();

        // Then
        Long actualCount = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(0L, actualCount);
        verify(transactionRepository).countAll();
    }

    @Test
    void testCountByTickerSuccess() {
        // Given
        String ticker = "AAPL";
        Long expectedCount = 5L;

        when(transactionRepository.countByTicker(ticker))
            .thenReturn(Uni.createFrom().item(expectedCount));

        // When
        Uni<Long> result = useCase.countByTicker(ticker);

        // Then
        Long actualCount = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(expectedCount, actualCount);
        verify(transactionRepository).countByTicker(ticker);
    }

    @Test
    void testCountByTickerZero() {
        // Given
        String ticker = "UNKNOWN";

        when(transactionRepository.countByTicker(ticker))
            .thenReturn(Uni.createFrom().item(0L));

        // When
        Uni<Long> result = useCase.countByTicker(ticker);

        // Then
        Long actualCount = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(0L, actualCount);
        verify(transactionRepository).countByTicker(ticker);
    }

    @ParameterizedTest
    @MethodSource("transactionTypeProvider")
    void testSearchTransactionsByType(TransactionType transactionType) {
        // Given
        List<Transaction> searchResults = List.of(
                createTransactionWithType(UUID.randomUUID(), "AAPL", transactionType)
        );

        when(transactionRepository.searchTransactions(null, transactionType, null, null))
            .thenReturn(Uni.createFrom().item(searchResults));

        // When
        Multi<Transaction> result = useCase.searchTransactions(null, transactionType, null, null);

        // Then
        List<Transaction> actualTransactions = result.collect().asList()
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(1, actualTransactions.size());
        assertEquals(transactionType, actualTransactions.getFirst().getTransactionType());
        verify(transactionRepository).searchTransactions(null, transactionType, null, null);
    }

    @ParameterizedTest
    @MethodSource("tickerProvider")
    void testGetByTickerWithDifferentTickers(String ticker) {
        // Given
        List<Transaction> transactions = List.of(
                createTransaction(UUID.randomUUID(), ticker)
        );

        when(transactionRepository.findByTicker(ticker))
            .thenReturn(Uni.createFrom().item(transactions));

        // When
        Multi<Transaction> result = useCase.getByTicker(ticker);

        // Then
        List<Transaction> actualTransactions = result.collect().asList()
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(1, actualTransactions.size());
        assertEquals(ticker, actualTransactions.getFirst().getTicker());
        verify(transactionRepository).findByTicker(ticker);
    }

    static Stream<Arguments> transactionTypeProvider() {
        return Stream.of(
            Arguments.of(TransactionType.BUY),
            Arguments.of(TransactionType.SELL),
            Arguments.of(TransactionType.DIVIDEND)
        );
    }

    static Stream<Arguments> tickerProvider() {
        return Stream.of(
            Arguments.of("AAPL"),
            Arguments.of("MSFT"),
            Arguments.of("GOOGL"),
            Arguments.of("TSLA"),
            Arguments.of("NVDA")
        );
    }

    private Transaction createTransaction(UUID id, String ticker) {
        return new Transaction(
            id,
            ticker,
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

    private Transaction createActiveTransaction(UUID id, String ticker) {
        Transaction base = createTransaction(id, ticker);
        return new Transaction(base.getId(), base.getTicker(), base.getTransactionType(), base.getQuantity(), 
                              base.getPrice(), base.getFees(), base.getCurrency(), base.getTransactionDate(), 
                              base.getNotes(), true, base.getIsFractional(), base.getFractionalMultiplier(), 
                              base.getCommissionCurrency(), Collections.emptyList());
    }

    private Transaction createInactiveTransaction(UUID id, String ticker) {
        Transaction base = createTransaction(id, ticker);
        return new Transaction(base.getId(), base.getTicker(), base.getTransactionType(), base.getQuantity(), 
                              base.getPrice(), base.getFees(), base.getCurrency(), base.getTransactionDate(), 
                              base.getNotes(), false, base.getIsFractional(), base.getFractionalMultiplier(), 
                              base.getCommissionCurrency(), Collections.emptyList());
    }

    private Transaction createTransactionWithType(UUID id, String ticker, TransactionType type) {
        return new Transaction(
            id,
            ticker,
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

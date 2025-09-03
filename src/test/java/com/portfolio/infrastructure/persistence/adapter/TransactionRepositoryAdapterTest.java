package com.portfolio.infrastructure.persistence.adapter;

import com.portfolio.domain.event.DomainEvent;
import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.infrastructure.persistence.entity.TransactionEntity;
import com.portfolio.infrastructure.persistence.mapper.TransactionEntityMapper;
import com.portfolio.infrastructure.persistence.repository.TransactionPanacheRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionRepositoryAdapterTest {
    private TransactionPanacheRepository panacheRepository;
    private TransactionEntityMapper transactionEntityMapper;
    private TransactionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        panacheRepository = mock(TransactionPanacheRepository.class);
        transactionEntityMapper = mock(TransactionEntityMapper.class);
        adapter = new TransactionRepositoryAdapter(panacheRepository, transactionEntityMapper);
    }

    @Test
    void testSave() {
        Transaction transaction = mock(Transaction.class);
        List<DomainEvent<?>> domainEvents = List.of(mock(DomainEvent.class));
        TransactionEntity entity = mock(TransactionEntity.class);
        when(transactionEntityMapper.toEntity(transaction)).thenReturn(entity);
        when(panacheRepository.persistAndFlush(entity)).thenReturn(Uni.createFrom().item(entity));
        when(transaction.getDomainEvents()).thenReturn(domainEvents);
        when(transactionEntityMapper.toDomain(entity, domainEvents)).thenReturn(transaction);

        Uni<Transaction> uni = adapter.save(transaction);
        Transaction result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertEquals(transaction, result);
        verify(transactionEntityMapper).toEntity(transaction);
        verify(transactionEntityMapper).toDomain(entity, domainEvents);
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        TransactionEntity entity = mock(TransactionEntity.class);
        Transaction transaction = mock(Transaction.class);
        when(panacheRepository.findById(id)).thenReturn(Uni.createFrom().item(entity));
        when(transactionEntityMapper.toDomain(entity)).thenReturn(transaction);

        Uni<Transaction> uni = adapter.findById(id);
        Transaction result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertEquals(transaction, result);
        verify(transactionEntityMapper).toDomain(entity);
    }

    @Test
    void testFindByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(panacheRepository.findById(id)).thenReturn(Uni.createFrom().item((TransactionEntity) null));

        Uni<Transaction> uni = adapter.findById(id);
        Transaction result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertNull(result);
    }

    @Test
    void testFindByTicker() {
        String ticker = "AAPL";
        TransactionEntity entity = mock(TransactionEntity.class);
        Transaction transaction = mock(Transaction.class);
        when(panacheRepository.findByTicker(ticker)).thenReturn(Uni.createFrom().item(List.of(entity)));
        when(transactionEntityMapper.toDomain(entity)).thenReturn(transaction);

        Uni<List<Transaction>> uni = adapter.findByTicker(ticker);
        List<Transaction> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transaction, result.getFirst());
        verify(transactionEntityMapper).toDomain(entity);
    }

    @Test
    void testFindAll() {
        TransactionEntity entity = mock(TransactionEntity.class);
        Transaction transaction = mock(Transaction.class);
        when(panacheRepository.findAllOrderedByDate()).thenReturn(Uni.createFrom().item(List.of(entity)));
        when(transactionEntityMapper.toDomain(entity)).thenReturn(transaction);

        Uni<List<Transaction>> uni = adapter.findAll();
        List<Transaction> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transaction, result.getFirst());
        verify(transactionEntityMapper).toDomain(entity);
    }

    @Test
    void testSearchTransactions() {
        String ticker = "AAPL";
        TransactionType type = TransactionType.BUY;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        TransactionEntity entity = mock(TransactionEntity.class);
        Transaction transaction = mock(Transaction.class);
        when(panacheRepository.searchTransactions(ticker, type, from, to)).thenReturn(Uni.createFrom().item(List.of(entity)));
        when(transactionEntityMapper.toDomain(entity)).thenReturn(transaction);

        Uni<List<Transaction>> uni = adapter.searchTransactions(ticker, type, from, to);
        List<Transaction> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transaction, result.getFirst());
        verify(transactionEntityMapper).toDomain(entity);
    }

    @Test
    void testUpdate() {
        Transaction transaction = mock(Transaction.class);
        List<DomainEvent<?>> domainEvents = Arrays.asList(mock(DomainEvent.class));
        TransactionEntity entity = mock(TransactionEntity.class);
        TransactionEntity mergedEntity = mock(TransactionEntity.class);
        Mutiny.Session session = mock(Mutiny.Session.class);

        when(transactionEntityMapper.toEntity(transaction)).thenReturn(entity);
        when(panacheRepository.getSession()).thenReturn(Uni.createFrom().item(session));
        when(session.merge(entity)).thenReturn(Uni.createFrom().item(mergedEntity));
        when(panacheRepository.persistAndFlush(mergedEntity)).thenReturn(Uni.createFrom().item(mergedEntity));
        when(transaction.popEvents()).thenReturn(domainEvents);
        when(transactionEntityMapper.toDomain(mergedEntity, domainEvents)).thenReturn(transaction);

        Uni<Transaction> uni = adapter.update(transaction);
        Transaction result = uni.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        assertEquals(transaction, result);
        verify(transactionEntityMapper).toEntity(transaction);
        verify(transactionEntityMapper).toDomain(mergedEntity, domainEvents);
        verify(panacheRepository).persistAndFlush(mergedEntity);
    }

    @Test
    void testDeleteById() {
        UUID id = UUID.randomUUID();
        when(panacheRepository.deleteById(id)).thenReturn(Uni.createFrom().item(true));

        Uni<Boolean> uni = adapter.deleteById(id);
        Boolean result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertTrue(result);
    }

    @Test
    void testExistsByIdTrue() {
        UUID id = UUID.randomUUID();
        TransactionEntity entity = mock(TransactionEntity.class);
        when(panacheRepository.findByIdActive(id)).thenReturn(Uni.createFrom().item(entity));

        Uni<Boolean> uni = adapter.existsById(id);
        Boolean result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertTrue(result);
    }

    @Test
    void testExistsByIdFalse() {
        UUID id = UUID.randomUUID();
        when(panacheRepository.findByIdActive(id)).thenReturn(Uni.createFrom().item((TransactionEntity) null));

        Uni<Boolean> uni = adapter.existsById(id);
        Boolean result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertFalse(result);
    }

    @Test
    void testCountAll() {
        when(panacheRepository.count()).thenReturn(Uni.createFrom().item(42L));

        Uni<Long> uni = adapter.countAll();
        Long result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertEquals(42L, result);
    }

    @Test
    void testCountByTicker() {
        String ticker = "AAPL";
        when(panacheRepository.countByTicker(ticker)).thenReturn(Uni.createFrom().item(7L));

        Uni<Long> uni = adapter.countByTicker(ticker);
        Long result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertEquals(7L, result);
    }
} 
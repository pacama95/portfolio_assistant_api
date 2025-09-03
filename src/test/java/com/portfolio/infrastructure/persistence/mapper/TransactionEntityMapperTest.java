package com.portfolio.infrastructure.persistence.mapper;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.infrastructure.persistence.entity.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TransactionEntityMapperTest {
    private TransactionEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TransactionEntityMapper.class);
    }

    @Test
    void testToEntityFullMapping() {
        UUID id = UUID.randomUUID();
        Transaction transaction = new Transaction(
            id,
            "AAPL",
            TransactionType.BUY,
            new BigDecimal("10.0"),
            new BigDecimal("100.0"),
            new BigDecimal("5.0"),
            Currency.USD,
            LocalDate.of(2024, 6, 1),
            "Test note",
            true,
            false,
            BigDecimal.ONE,
            Currency.USD,
                Collections.emptyList()
        );

        TransactionEntity entity = mapper.toEntity(transaction);
        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals("AAPL", entity.getTicker());
        assertEquals(TransactionType.BUY, entity.getTransactionType());
        assertEquals(new BigDecimal("10.0"), entity.getQuantity());
        assertEquals(new BigDecimal("100.0"), entity.getCostPerShare());
        assertEquals(new BigDecimal("5.0"), entity.getCommission());
        assertEquals(Currency.USD, entity.getCurrency());
        assertEquals(LocalDate.of(2024, 6, 1), entity.getTransactionDate());
        assertEquals("Test note", entity.getNotes());
        assertEquals(Currency.USD, entity.getCommissionCurrency());
        assertEquals(false, entity.getIsFractional());
        assertEquals(BigDecimal.ONE, entity.getFractionalMultiplier());
    }

    @Test
    void testToEntityWithNulls() {
        Transaction transaction = new Transaction(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
                Collections.emptyList()
        );
        TransactionEntity entity = mapper.toEntity(transaction);
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getTicker());
        assertNull(entity.getTransactionType());
        assertNull(entity.getQuantity());
        assertNull(entity.getCostPerShare());
        assertNull(entity.getCommission());
        assertNull(entity.getCurrency());
        assertNull(entity.getTransactionDate());
        assertNull(entity.getNotes());
        assertNull(entity.getCommissionCurrency());
        assertNull(entity.getIsFractional());
        assertNull(entity.getFractionalMultiplier());
    }

    @Test
    void testToDomainFullMapping() {
        TransactionEntity entity = new TransactionEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setTicker("AAPL");
        entity.setTransactionType(TransactionType.SELL);
        entity.setQuantity(new BigDecimal("20.0"));
        entity.setCostPerShare(new BigDecimal("150.0"));
        entity.setCommission(new BigDecimal("2.5"));
        entity.setCurrency(Currency.CAD);
        entity.setTransactionDate(LocalDate.of(2024, 5, 15));
        entity.setNotes("Domain test");
        entity.setCommissionCurrency(Currency.EUR);
        entity.setIsFractional(true);
        entity.setFractionalMultiplier(new BigDecimal("0.5"));

        Transaction transaction = mapper.toDomain(entity);
        assertNotNull(transaction);
        assertEquals(id, transaction.getId());
        assertEquals("AAPL", transaction.getTicker());
        assertEquals(TransactionType.SELL, transaction.getTransactionType());
        assertEquals(new BigDecimal("20.0"), transaction.getQuantity());
        assertEquals(new BigDecimal("150.0"), transaction.getPrice());
        assertEquals(new BigDecimal("2.5"), transaction.getFees());
        assertEquals(Currency.CAD, transaction.getCurrency());
        assertEquals(LocalDate.of(2024, 5, 15), transaction.getTransactionDate());
        assertEquals("Domain test", transaction.getNotes());
        assertEquals(Currency.EUR, transaction.getCommissionCurrency());
        assertEquals(true, transaction.getIsFractional());
        assertEquals(new BigDecimal("0.5"), transaction.getFractionalMultiplier());
    }

    @ParameterizedTest
    @MethodSource("provideTransactionEntityMappingTestCases")
    void testFieldMappings(String testName, Transaction transaction, TransactionEntity expectedEntity) {
        TransactionEntity actualEntity = mapper.toEntity(transaction);
        
        assertNotNull(actualEntity, testName + ": Entity should not be null");
        assertEquals(expectedEntity.getId(), actualEntity.getId(), testName + ": ID mismatch");
        assertEquals(expectedEntity.getTicker(), actualEntity.getTicker(), testName + ": Ticker mismatch");
        assertEquals(expectedEntity.getTransactionType(), actualEntity.getTransactionType(), testName + ": TransactionType mismatch");
        assertEquals(expectedEntity.getQuantity(), actualEntity.getQuantity(), testName + ": Quantity mismatch");
        assertEquals(expectedEntity.getCostPerShare(), actualEntity.getCostPerShare(), testName + ": Price mapping mismatch");
        assertEquals(expectedEntity.getCommission(), actualEntity.getCommission(), testName + ": Fees mapping mismatch");
        assertEquals(expectedEntity.getCurrency(), actualEntity.getCurrency(), testName + ": Currency mismatch");
        assertEquals(expectedEntity.getTransactionDate(), actualEntity.getTransactionDate(), testName + ": TransactionDate mismatch");
        assertEquals(expectedEntity.getNotes(), actualEntity.getNotes(), testName + ": Notes mismatch");
        assertEquals(expectedEntity.getCommissionCurrency(), actualEntity.getCommissionCurrency(), testName + ": CommissionCurrency mismatch");
        assertEquals(expectedEntity.getIsFractional(), actualEntity.getIsFractional(), testName + ": IsFractional mismatch");
        assertEquals(expectedEntity.getFractionalMultiplier(), actualEntity.getFractionalMultiplier(), testName + ": FractionalMultiplier mismatch");
    }

    static Stream<Arguments> provideTransactionEntityMappingTestCases() {
        UUID id1 = UUID.randomUUID();
        Transaction transaction1 = new Transaction(
            id1, "TSLA", TransactionType.BUY, new BigDecimal("5.0"), new BigDecimal("250.0"), 
            new BigDecimal("1.0"), Currency.EUR, LocalDate.of(2024, 3, 1), "Buy Tesla", 
            true, true, new BigDecimal("2.0"), Currency.GBP, Collections.emptyList()
        );
        TransactionEntity expectedEntity1 = new TransactionEntity();
        expectedEntity1.setId(id1);
        expectedEntity1.setTicker("TSLA");
        expectedEntity1.setTransactionType(TransactionType.BUY);
        expectedEntity1.setQuantity(new BigDecimal("5.0"));
        expectedEntity1.setCostPerShare(new BigDecimal("250.0"));
        expectedEntity1.setCommission(new BigDecimal("1.0"));
        expectedEntity1.setCurrency(Currency.EUR);
        expectedEntity1.setTransactionDate(LocalDate.of(2024, 3, 1));
        expectedEntity1.setNotes("Buy Tesla");
        expectedEntity1.setCommissionCurrency(Currency.GBP);
        expectedEntity1.setIsFractional(true);
        expectedEntity1.setFractionalMultiplier(new BigDecimal("2.0"));

        UUID id2 = UUID.randomUUID();
        Transaction transaction2 = new Transaction(
            id2, "GOOGL", TransactionType.SELL, new BigDecimal("1.5"), new BigDecimal("3000.0"), 
            new BigDecimal("15.0"), Currency.USD, LocalDate.of(2024, 4, 15), "Sell Google", 
            true, false, BigDecimal.ONE, null, Collections.emptyList()
        );
        TransactionEntity expectedEntity2 = new TransactionEntity();
        expectedEntity2.setId(id2);
        expectedEntity2.setTicker("GOOGL");
        expectedEntity2.setTransactionType(TransactionType.SELL);
        expectedEntity2.setQuantity(new BigDecimal("1.5"));
        expectedEntity2.setCostPerShare(new BigDecimal("3000.0"));
        expectedEntity2.setCommission(new BigDecimal("15.0"));
        expectedEntity2.setCurrency(Currency.USD);
        expectedEntity2.setTransactionDate(LocalDate.of(2024, 4, 15));
        expectedEntity2.setNotes("Sell Google");
        expectedEntity2.setCommissionCurrency(null);
        expectedEntity2.setIsFractional(false);
        expectedEntity2.setFractionalMultiplier(BigDecimal.ONE);

        return Stream.of(
            Arguments.of("Tesla transaction with fractional and commission currency", transaction1, expectedEntity1),
            Arguments.of("Google transaction without commission currency", transaction2, expectedEntity2)
        );
    }
} 
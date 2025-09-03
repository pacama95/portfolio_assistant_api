package com.portfolio.infrastructure.persistence.adapter;

import com.portfolio.domain.model.Position;
import com.portfolio.infrastructure.persistence.entity.PositionEntity;
import com.portfolio.infrastructure.persistence.repository.PositionPanacheRepository;
import com.portfolio.infrastructure.persistence.mapper.PositionEntityMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PositionRepositoryAdapterTest {
    private PositionPanacheRepository panacheRepository;
    private PositionEntityMapper positionEntityMapper;
    private PositionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        panacheRepository = mock(PositionPanacheRepository.class);
        positionEntityMapper = mock(PositionEntityMapper.class);
        adapter = new PositionRepositoryAdapter(panacheRepository, positionEntityMapper);
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        PositionEntity entity = mock(PositionEntity.class);
        Position position = mock(Position.class);
        when(panacheRepository.findById(id)).thenReturn(Uni.createFrom().item(entity));
        when(positionEntityMapper.toDomain(entity)).thenReturn(position);

        Uni<Position> uni = adapter.findById(id);
        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertEquals(position, result);
        verify(positionEntityMapper).toDomain(entity);
    }

    @Test
    void testFindByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(panacheRepository.findById(id)).thenReturn(Uni.createFrom().item((PositionEntity) null));
        Uni<Position> uni = adapter.findById(id);
        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();
        assertNull(result);
    }

    @Test
    void testFindByTicker() {
        String ticker = "AAPL";
        PositionEntity entity = mock(PositionEntity.class);
        Position position = mock(Position.class);
        when(panacheRepository.findByTicker(ticker)).thenReturn(Uni.createFrom().item(entity));
        when(positionEntityMapper.toDomain(entity)).thenReturn(position);

        Uni<Position> uni = adapter.findByTicker(ticker);
        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertEquals(position, result);
        verify(positionEntityMapper).toDomain(entity);
    }

    @Test
    void testFindByTickerNotFound() {
        String ticker = "AAPL";
        when(panacheRepository.findByTicker(ticker)).thenReturn(Uni.createFrom().item((PositionEntity) null));

        Uni<Position> uni = adapter.findByTicker(ticker);
        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertNull(result);
    }

    @Test
    void testFindAllWithShares() {
        PositionEntity entity = mock(PositionEntity.class);
        Position position = mock(Position.class);
        when(panacheRepository.findAllWithShares()).thenReturn(Uni.createFrom().item(List.of(entity)));
        when(positionEntityMapper.toDomain(entity)).thenReturn(position);

        Uni<List<Position>> uni = adapter.findAllWithShares();
        List<Position> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(position, result.getFirst());
        verify(positionEntityMapper).toDomain(entity);
    }

    @Test
    void testFindAll() {
        PositionEntity entity = mock(PositionEntity.class);
        Position position = mock(Position.class);
        when(panacheRepository.findAllActive()).thenReturn(Uni.createFrom().item(List.of(entity)));
        when(positionEntityMapper.toDomain(entity)).thenReturn(position);

        Uni<List<Position>> uni = adapter.findAll();
        List<Position> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(position, result.getFirst());
        verify(positionEntityMapper).toDomain(entity);
    }

    @Test
    void testUpdateMarketPrice() {
        String ticker = "AAPL";
        BigDecimal price = new BigDecimal("123.45");
        PositionEntity entity = mock(PositionEntity.class);
        Position position = mock(Position.class);
        when(panacheRepository.updateMarketPrice(ticker, price)).thenReturn(Uni.createFrom().item(entity));
        when(positionEntityMapper.toDomain(entity)).thenReturn(position);

        Uni<Position> uni = adapter.updateMarketPrice(ticker, price);
        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertEquals(position, result);
        verify(positionEntityMapper).toDomain(entity);
    }

    @Test
    void testUpdateMarketPriceNotFound() {
        String ticker = "AAPL";
        BigDecimal price = new BigDecimal("123.45");
        when(panacheRepository.updateMarketPrice(ticker, price)).thenReturn(Uni.createFrom().item((PositionEntity) null));

        Uni<Position> uni = adapter.updateMarketPrice(ticker, price);
        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertNull(result);
    }

    @Test
    void testRecalculatePosition() {
        String ticker = "AAPL";
        PositionEntity entity = mock(PositionEntity.class);
        Position position = mock(Position.class);
        when(panacheRepository.recalculatePosition(ticker)).thenReturn(Uni.createFrom().item(entity));
        when(positionEntityMapper.toDomain(entity)).thenReturn(position);

        Uni<Position> uni = adapter.recalculatePosition(ticker);
        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertEquals(position, result);
        verify(positionEntityMapper).toDomain(entity);
    }

    @Test
    void testRecalculatePositionNotFound() {
        String ticker = "AAPL";
        when(panacheRepository.recalculatePosition(ticker)).thenReturn(Uni.createFrom().item((PositionEntity) null));

        Uni<Position> uni = adapter.recalculatePosition(ticker);
        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertNull(result);
    }

    @Test
    void testExistsByTicker() {
        String ticker = "AAPL";
        when(panacheRepository.existsByTicker(ticker)).thenReturn(Uni.createFrom().item(true));

        Uni<Boolean> uni = adapter.existsByTicker(ticker);
        Boolean result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertTrue(result);
    }

    @Test
    void testCountAll() {
        when(panacheRepository.count()).thenReturn(Uni.createFrom().item(42L));

        Uni<Long> uni = adapter.countAll();
        Long result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertEquals(42L, result);
    }

    @Test
    void testCountWithShares() {
        when(panacheRepository.countWithShares()).thenReturn(Uni.createFrom().item(7L));

        Uni<Long> uni = adapter.countWithShares();
        Long result = uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted().getItem();

        assertEquals(7L, result);
    }
} 
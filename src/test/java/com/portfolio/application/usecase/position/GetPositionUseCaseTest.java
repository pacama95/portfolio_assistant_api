package com.portfolio.application.usecase.position;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.CurrentPosition;
import com.portfolio.domain.model.Position;
import com.portfolio.domain.port.MarketDataService;
import com.portfolio.domain.port.PositionRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetPositionUseCaseTest {
    private PositionRepository positionRepository;
    private MarketDataService marketDataService;
    private GetPositionUseCase useCase;

    @BeforeEach
    void setUp() {
        positionRepository = mock(PositionRepository.class);
        marketDataService = mock(MarketDataService.class);
        useCase = new GetPositionUseCase();
        useCase.positionRepository = positionRepository;
        useCase.marketDataService = marketDataService;
    }

    @Test
    void testGetByIdSuccess() {
        // Given
        UUID id = UUID.randomUUID();
        Position position = createTestPosition("AAPL");
        BigDecimal realTimePrice = new BigDecimal("175.50");
        
        when(positionRepository.findById(id)).thenReturn(Uni.createFrom().item(position));
        when(marketDataService.getCurrentPrice("AAPL")).thenReturn(Uni.createFrom().item(realTimePrice));

        // When
        Uni<CurrentPosition> uni = useCase.getById(id);
        CurrentPosition result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(position.getId(), result.getId());
        assertEquals(position.getTicker(), result.getTicker());
        assertEquals(realTimePrice, result.getCurrentPrice());
        assertTrue(result.isCurrentPriceFresh());
        verify(marketDataService).getCurrentPrice("AAPL");
    }

    @Test
    void testGetByIdWithMarketDataServiceFailure() {
        // Given
        UUID id = UUID.randomUUID();
        Position position = createTestPosition("AAPL");
        RuntimeException marketDataException = new RuntimeException("Market data API error");
        
        when(positionRepository.findById(id)).thenReturn(Uni.createFrom().item(position));
        when(marketDataService.getCurrentPrice("AAPL")).thenReturn(Uni.createFrom().failure(marketDataException));

        // When
        Uni<CurrentPosition> uni = useCase.getById(id);
        CurrentPosition result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(position.getId(), result.getId());
        assertEquals(position.getTicker(), result.getTicker());
        assertEquals(position.getCurrentPrice(), result.getCurrentPrice()); // Fallback to stored price
        assertFalse(result.isCurrentPriceFresh()); // Should use stored price timestamp
        verify(marketDataService).getCurrentPrice("AAPL");
    }

    @Test
    void testGetByIdRepositoryError() {
        // Given
        UUID id = UUID.randomUUID();
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.findById(id)).thenReturn(Uni.createFrom().failure(repoException));

        // When
        Uni<CurrentPosition> uni = useCase.getById(id);
        UniAssertSubscriber<CurrentPosition> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains(id.toString()));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testGetByTickerSuccess() {
        // Given
        String ticker = "MSFT";
        Position position = createTestPosition(ticker);
        BigDecimal realTimePrice = new BigDecimal("300.25");

        when(positionRepository.findByTicker(ticker)).thenReturn(Uni.createFrom().item(position));
        when(marketDataService.getCurrentPrice(ticker)).thenReturn(Uni.createFrom().item(realTimePrice));

        // When
        Uni<CurrentPosition> uni = useCase.getByTicker(ticker);
        CurrentPosition result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(position.getTicker(), result.getTicker());
        assertEquals(realTimePrice, result.getCurrentPrice());
        assertTrue(result.isCurrentPriceFresh());
        verify(marketDataService).getCurrentPrice(ticker);
    }

    @Test
    void testGetByTickerWithMarketDataFailure() {
        // Given
        String ticker = "GOOGL";
        Position position = createTestPosition(ticker);
        RuntimeException marketDataException = new RuntimeException("API rate limit exceeded");
        
        when(positionRepository.findByTicker(ticker)).thenReturn(Uni.createFrom().item(position));
        when(marketDataService.getCurrentPrice(ticker)).thenReturn(Uni.createFrom().failure(marketDataException));

        // When
        Uni<CurrentPosition> uni = useCase.getByTicker(ticker);
        CurrentPosition result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(position.getTicker(), result.getTicker());
        assertEquals(position.getCurrentPrice(), result.getCurrentPrice()); // Fallback to stored price
        assertFalse(result.isCurrentPriceFresh());
    }

    @Test
    void testGetByTickerError() {
        // Given
        String ticker = "AAPL";
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.findByTicker(ticker)).thenReturn(Uni.createFrom().failure(repoException));

        // When
        Uni<CurrentPosition> uni = useCase.getByTicker(ticker);
        UniAssertSubscriber<CurrentPosition> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains(ticker));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testGetAllSuccess() {
        // Given
        Position position1 = createTestPosition("AAPL");
        Position position2 = createTestPosition("MSFT");
        List<Position> positions = List.of(position1, position2);
        
        when(positionRepository.findAll()).thenReturn(Uni.createFrom().item(positions));
        when(marketDataService.getCurrentPrice("AAPL")).thenReturn(Uni.createFrom().item(new BigDecimal("175.50")));
        when(marketDataService.getCurrentPrice("MSFT")).thenReturn(Uni.createFrom().item(new BigDecimal("300.25")));

        // When
        Uni<List<CurrentPosition>> uni = useCase.getAll();
        List<CurrentPosition> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        assertEquals("MSFT", result.get(1).getTicker());
        assertEquals(new BigDecimal("175.50"), result.get(0).getCurrentPrice());
        assertEquals(new BigDecimal("300.25"), result.get(1).getCurrentPrice());
        verify(marketDataService).getCurrentPrice("AAPL");
        verify(marketDataService).getCurrentPrice("MSFT");
    }

    @Test
    void testGetAllWithEmptyList() {
        // Given
        when(positionRepository.findAll()).thenReturn(Uni.createFrom().item(Collections.emptyList()));

        // When
        Uni<List<CurrentPosition>> uni = useCase.getAll();
        List<CurrentPosition> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(marketDataService);
    }

    @Test
    void testGetAllError() {
        // Given
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.findAll()).thenReturn(Uni.createFrom().failure(repoException));

        // When
        Uni<List<CurrentPosition>> uni = useCase.getAll();
        UniAssertSubscriber<List<CurrentPosition>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("all positions"));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testGetActivePositionsSuccess() {
        // Given
        Position position = createTestPosition("TSLA");
        List<Position> positions = Collections.singletonList(position);
        
        when(positionRepository.findAllWithShares()).thenReturn(Uni.createFrom().item(positions));
        when(marketDataService.getCurrentPrice("TSLA")).thenReturn(Uni.createFrom().item(new BigDecimal("800.75")));

        // When
        Uni<List<CurrentPosition>> uni = useCase.getActivePositions();
        List<CurrentPosition> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TSLA", result.get(0).getTicker());
        assertEquals(new BigDecimal("800.75"), result.get(0).getCurrentPrice());
        verify(marketDataService).getCurrentPrice("TSLA");
    }

    @Test
    void testGetActivePositionsWithMixedMarketDataResults() {
        // Given
        Position position1 = createTestPosition("AAPL");
        Position position2 = createTestPosition("MSFT");
        List<Position> positions = List.of(position1, position2);
        
        when(positionRepository.findAllWithShares()).thenReturn(Uni.createFrom().item(positions));
        when(marketDataService.getCurrentPrice("AAPL")).thenReturn(Uni.createFrom().item(new BigDecimal("175.50")));
        when(marketDataService.getCurrentPrice("MSFT")).thenReturn(Uni.createFrom().failure(new RuntimeException("API error")));

        // When
        Uni<List<CurrentPosition>> uni = useCase.getActivePositions();
        List<CurrentPosition> result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // First position should have real-time price
        assertEquals("AAPL", result.get(0).getTicker());
        assertEquals(new BigDecimal("175.50"), result.get(0).getCurrentPrice());
        assertTrue(result.get(0).isCurrentPriceFresh());
        // Second position should have fallback stored price
        assertEquals("MSFT", result.get(1).getTicker());
        assertEquals(position2.getCurrentPrice(), result.get(1).getCurrentPrice());
        assertFalse(result.get(1).isCurrentPriceFresh());
    }

    @Test
    void testGetActivePositionsError() {
        // Given
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.findAllWithShares()).thenReturn(Uni.createFrom().failure(repoException));

        // When
        Uni<List<CurrentPosition>> uni = useCase.getActivePositions();
        UniAssertSubscriber<List<CurrentPosition>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("active positions"));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testExistsByTickerSuccess() {
        // Given
        String ticker = "AMZN";
        when(positionRepository.existsByTicker(ticker)).thenReturn(Uni.createFrom().item(true));

        // When
        Uni<Boolean> uni = useCase.existsByTicker(ticker);
        Boolean result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertTrue(result);
    }

    @Test
    void testExistsByTickerError() {
        // Given
        String ticker = "AAPL";
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.existsByTicker(ticker)).thenReturn(Uni.createFrom().failure(repoException));

        // When
        Uni<Boolean> uni = useCase.existsByTicker(ticker);
        UniAssertSubscriber<Boolean> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains(ticker));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testCountAllSuccess() {
        // Given
        when(positionRepository.countAll()).thenReturn(Uni.createFrom().item(42L));

        // When
        Uni<Long> uni = useCase.countAll();
        Long result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(42L, result);
    }

    @Test
    void testCountAllError() {
        // Given
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.countAll()).thenReturn(Uni.createFrom().failure(repoException));

        // When
        Uni<Long> uni = useCase.countAll();
        UniAssertSubscriber<Long> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("positions  count"));
        assertEquals(repoException, thrown.getCause());
    }

    @Test
    void testCountActivePositionsSuccess() {
        // Given
        when(positionRepository.countWithShares()).thenReturn(Uni.createFrom().item(7L));

        // When
        Uni<Long> uni = useCase.countActivePositions();
        Long result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertEquals(7L, result);
    }

    @Test
    void testCountActivePositionsError() {
        // Given
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.countWithShares()).thenReturn(Uni.createFrom().failure(repoException));

        // When
        Uni<Long> uni = useCase.countActivePositions();
        UniAssertSubscriber<Long> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.GetPosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains("active positions count"));
        assertEquals(repoException, thrown.getCause());
    }

    @ParameterizedTest
    @MethodSource("fallbackScenarios")
    void testMarketDataFallbackScenarios(Position position, BigDecimal expectedFallback) {
        // Given
        UUID id = UUID.randomUUID();
        RuntimeException marketDataException = new RuntimeException("Service unavailable");

        when(positionRepository.findById(id)).thenReturn(Uni.createFrom().item(position));
        when(marketDataService.getCurrentPrice(position.getTicker())).thenReturn(Uni.createFrom().failure(marketDataException));

        // When
        Uni<CurrentPosition> uni = useCase.getById(id);
        CurrentPosition result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        // Then
        assertNotNull(result);
        assertEquals(expectedFallback, result.getCurrentPrice());
        assertFalse(result.isCurrentPriceFresh());

        verify(marketDataService).getCurrentPrice(position.getTicker());
    }

    @Test
    void testMarketDataFallbackScenarios() {
        // Given
        UUID id = UUID.randomUUID();

        when(positionRepository.findById(id)).thenReturn(Uni.createFrom().nullItem());

        // When
        Uni<CurrentPosition> uni = useCase.getById(id);
        CurrentPosition result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertNull(result);

        verifyNoInteractions(marketDataService);
    }

    static Stream<Arguments> fallbackScenarios() {
        Position positionWithStoredPrice = createTestPosition("AAPL");
        positionWithStoredPrice.setCurrentPrice(new BigDecimal("160.00"));
        
        Position positionWithNullPrice = createTestPosition("MSFT");
        positionWithNullPrice.setCurrentPrice(null);
        
        return Stream.of(
            Arguments.of(positionWithStoredPrice, new BigDecimal("160.00")),
            Arguments.of(positionWithNullPrice, BigDecimal.ZERO)
        );
    }

    private static Position createTestPosition(String ticker) {
        Position position = new Position();
        position.setId(UUID.randomUUID());
        position.setTicker(ticker);
        position.setTotalQuantity(new BigDecimal("100"));
        position.setAveragePrice(new BigDecimal("150.00"));
        position.setCurrentPrice(new BigDecimal("160.00"));
        position.setTotalCost(new BigDecimal("15000.00"));
        position.setCurrency(Currency.USD);
        position.setLastUpdated(LocalDate.now().minusDays(1));
        position.setIsActive(true);
        return position;
    }
}
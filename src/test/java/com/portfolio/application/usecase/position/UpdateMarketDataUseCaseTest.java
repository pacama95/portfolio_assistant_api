package com.portfolio.application.usecase.position;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Position;
import com.portfolio.domain.port.PositionRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.util.stream.Stream;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateMarketDataUseCaseTest {
    private PositionRepository positionRepository;
    private UpdateMarketDataUseCase useCase;

    @BeforeEach
    void setUp() {
        positionRepository = mock(PositionRepository.class);
        useCase = new UpdateMarketDataUseCase();
        useCase.positionRepository = positionRepository;
    }

    @ParameterizedTest
    @MethodSource("invalidInputProvider")
    void testInputValidation(String ticker, BigDecimal price, String expectedMessage) {
        Uni<Position> uni = useCase.execute(ticker, price);
        UniAssertSubscriber<Position> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.UpdateMarketData.INVALID_INPUT, thrown.getError());
        assertEquals(expectedMessage, thrown.getMessage());
    }

    static Stream<Arguments> invalidInputProvider() {
        return Stream.of(
            Arguments.of(null, BigDecimal.ONE, "Ticker cannot be null or empty"),
            Arguments.of("   ", BigDecimal.ONE, "Ticker cannot be null or empty"),
            Arguments.of("AAPL", null, "Price must be positive"),
            Arguments.of("AAPL", BigDecimal.ZERO, "Price must be positive")
        );
    }

    @Test
    void testExecuteNotFound() {
        String ticker = "AAPL";
        BigDecimal price = new BigDecimal("123.45");
        when(positionRepository.existsByTicker(ticker)).thenReturn(Uni.createFrom().item(false));

        Uni<Position> uni = useCase.execute(ticker, price);
        UniAssertSubscriber<Position> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.UpdateMarketData.NOT_FOUND, thrown.getError());
        assertTrue(thrown.getMessage().contains(ticker));
    }

    @Test
    void testExecuteSuccess() {
        String ticker = "AAPL";
        BigDecimal price = new BigDecimal("123.45");
        Position position = mock(Position.class);
        when(positionRepository.existsByTicker(ticker)).thenReturn(Uni.createFrom().item(true));
        when(positionRepository.updateMarketPrice(ticker, price)).thenReturn(Uni.createFrom().item(position));

        Uni<Position> uni = useCase.execute(ticker, price);
        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(position, result);
    }

    @Test
    void testExecutePersistenceError() {
        String ticker = "AAPL";
        BigDecimal price = new BigDecimal("123.45");
        when(positionRepository.existsByTicker(ticker)).thenReturn(Uni.createFrom().item(true));
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.updateMarketPrice(ticker, price)).thenReturn(Uni.createFrom().failure(repoException));

        Uni<Position> uni = useCase.execute(ticker, price);
        UniAssertSubscriber<Position> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.UpdateMarketData.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains(ticker));
        assertTrue(thrown.getMessage().contains(price.toString()));
        assertEquals(repoException, thrown.getCause());
    }
} 
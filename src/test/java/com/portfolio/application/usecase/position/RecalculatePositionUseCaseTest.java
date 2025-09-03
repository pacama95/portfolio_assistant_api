package com.portfolio.application.usecase.position;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Position;
import com.portfolio.domain.port.PositionRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecalculatePositionUseCaseTest {
    private PositionRepository positionRepository;
    private RecalculatePositionUseCase useCase;

    @BeforeEach
    void setUp() {
        positionRepository = mock(PositionRepository.class);
        useCase = new RecalculatePositionUseCase();
        useCase.positionRepository = positionRepository;
    }

    @Test
    void testExecuteWithNullTicker() {
        Uni<Position> uni = useCase.execute(null);

        UniAssertSubscriber<Position> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();

        assertEquals(Errors.RecalculatePosition.INVALID_INPUT, thrown.getError());
        assertEquals("Ticker cannot be null or empty", thrown.getMessage());
    }

    @Test
    void testExecuteWithEmptyTicker() {
        Uni<Position> uni = useCase.execute("   ");

        UniAssertSubscriber<Position> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();

        assertEquals(Errors.RecalculatePosition.INVALID_INPUT, thrown.getError());
        assertEquals("Ticker cannot be null or empty", thrown.getMessage());
    }

    @Test
    void testExecuteSuccess() {
        String ticker = "AAPL";
        Position position = mock(Position.class);
        when(positionRepository.recalculatePosition(ticker)).thenReturn(Uni.createFrom().item(position));

        Uni<Position> uni = useCase.execute(ticker);
        Position result = uni.subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertEquals(position, result);
    }

    @Test
    void testExecutePersistenceError() {
        String ticker = "AAPL";
        RuntimeException repoException = new RuntimeException("DB error");
        when(positionRepository.recalculatePosition(ticker)).thenReturn(Uni.createFrom().failure(repoException));

        Uni<Position> uni = useCase.execute(ticker);
        UniAssertSubscriber<Position> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        ServiceException thrown = (ServiceException) subscriber.assertFailedWith(ServiceException.class).getFailure();
        assertEquals(Errors.RecalculatePosition.PERSISTENCE_ERROR, thrown.getError());
        assertTrue(thrown.getMessage().contains(ticker));
        assertEquals(repoException, thrown.getCause());
    }
} 
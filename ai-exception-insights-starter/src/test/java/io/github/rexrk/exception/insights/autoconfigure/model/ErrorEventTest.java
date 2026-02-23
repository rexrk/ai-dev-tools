package io.github.rexrk.exception.insights.autoconfigure.model;

import io.github.rexrk.exception.insights.model.ErrorEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorEventTest {

    @Test
    void buildWithoutException_shouldFail() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> ErrorEvent.builder()
                        .type(ErrorEvent.Type.HTTP_REQUEST)
                        .build()
        );

        assertEquals("Call .exception(Throwable) before .build()", ex.getMessage());
    }

    @Test
    void buildWithoutType_shouldFail() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> ErrorEvent.builder()
                        .exception(new RuntimeException("test"))
                        .build()
        );

        assertEquals("ErrorEvent.Type is required", ex.getMessage());
    }

    @Test
    void aiExplanationIsNullInitially() {
        ErrorEvent event = ErrorEvent.builder()
                .type(ErrorEvent.Type.ASYNC)
                .exception(new RuntimeException("test"))
                .build();

        assertNull(event.getAiExplanation());
    }
}
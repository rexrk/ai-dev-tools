package io.github.rexrk.exception.insights.model;

import java.time.Instant;

public record LogLine(
        String level,
        String message,
        String loggerName,
        String threadName,
        String throwableClass,
        String throwableMessage,
        Instant timestamp
) {}
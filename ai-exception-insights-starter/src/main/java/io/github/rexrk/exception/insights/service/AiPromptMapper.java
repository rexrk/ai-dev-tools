package io.github.rexrk.exception.insights.service;

import io.github.rexrk.exception.insights.model.AiPromptContext;
import io.github.rexrk.exception.insights.model.ErrorEvent;
import io.github.rexrk.exception.insights.model.LogLine;

import java.util.List;

public final class AiPromptMapper {

    private static final int MAX_STACK_FRAMES = 8;
    private static final int MAX_LOG_LINES = 8;

    private AiPromptMapper() {}

    public static AiPromptContext from(ErrorEvent event) {
        return new AiPromptContext(
            event.getExceptionClass(),
            event.getMessage(),
            topStackFrames(event.getStackTrace()),
            recentLogs(event.getRecentLogs()),
            executionContext(event)
        );
    }

    private static List<String> topStackFrames(String stackTrace) {
        if (stackTrace == null) return List.of();
        return stackTrace.lines()
            .filter(l -> l.startsWith("\tat "))
            .limit(MAX_STACK_FRAMES)
            .toList();
    }

    private static List<String> recentLogs(List<LogLine> logs) {
        return logs.stream()
            .filter(l -> "ERROR".equals(l.level()) || "WARN".equals(l.level()))
            .limit(MAX_LOG_LINES)
            .map(l -> l.level() + " " + l.loggerName() + " - " + l.message())
            .toList();
    }

    private static String executionContext(ErrorEvent event) {
        return switch (event.getType()) {
            case HTTP_REQUEST ->
                event.getHttpMethod() + " " + event.getRequestUri();
            case SCHEDULED ->
                "Scheduled task";
            case ASYNC ->
                "Async execution";
            case UNCAUGHT_THREAD ->
                "Uncaught thread exception";
            default ->
                event.getType().name();
        };
    }
}
package io.github.rexrk.exception.insights.model;

public record DashboardEvent(
        String id,
        String type,
        String exceptionClass,
        String timestamp
) {}
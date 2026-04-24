package io.github.rexrk.exception.insights.service.output.ui;

import io.github.rexrk.exception.insights.model.DashboardEvent;
import io.github.rexrk.exception.insights.model.ErrorEvent;
import io.github.rexrk.exception.insights.service.output.ErrorOutput;
import io.github.rexrk.ui.dashboard.SseEmitterRegistry;

import java.time.LocalDateTime;

public class UiErrorOutput implements ErrorOutput {

    private final SseEmitterRegistry registry;

    public UiErrorOutput(SseEmitterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onErrorCaptured(ErrorEvent event) {
        DashboardEvent dashboardEvent = new DashboardEvent(
                event.getType().name(),
                event.getExceptionClass(),
                LocalDateTime.now().toString()
        );
        registry.broadcast("error-captured", dashboardEvent);

    }

    @Override
    public void onAiExplanationReady(ErrorEvent event) {
        DashboardEvent dashboardEvent = new DashboardEvent(
                event.getType().name(),
                event.getExceptionClass(),
                LocalDateTime.now().toString()
        );
        registry.broadcast("ai-insight-ready", dashboardEvent);
    }
}
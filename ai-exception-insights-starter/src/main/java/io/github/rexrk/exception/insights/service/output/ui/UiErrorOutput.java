package io.github.rexrk.exception.insights.service.output.ui;

import io.github.rexrk.exception.insights.model.ErrorEvent;
import io.github.rexrk.exception.insights.service.output.ErrorOutput;

public class UiErrorOutput implements ErrorOutput {

//    private final SseEmitterRegistry registry;
//
//    public UiErrorOutput(SseEmitterRegistry registry) {
//        this.registry = registry;
//    }

    @Override
    public void onErrorCaptured(ErrorEvent event) {
//        registry.broadcast("error-captured", event);

    }

    @Override
    public void onAiExplanationReady(ErrorEvent event) {
//        registry.broadcast("ai-insight-ready", event);
    }
}
package io.github.rexrk.exception.insights.service.output;

import io.github.rexrk.exception.insights.model.ErrorEvent;

public interface ErrorOutput {
    void onErrorCaptured(ErrorEvent event);
    void onAiExplanationReady(ErrorEvent event);
}
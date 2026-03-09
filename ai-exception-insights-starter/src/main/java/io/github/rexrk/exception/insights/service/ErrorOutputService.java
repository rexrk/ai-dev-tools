package io.github.rexrk.exception.insights.service;

import io.github.rexrk.exception.insights.model.ErrorEvent;

public interface ErrorOutputService {
    void onErrorCaptured(ErrorEvent event);
    void onAiExplanationReady(ErrorEvent event);
}
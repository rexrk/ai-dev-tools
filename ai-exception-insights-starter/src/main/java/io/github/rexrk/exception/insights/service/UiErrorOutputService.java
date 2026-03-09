package io.github.rexrk.exception.insights.service;

import io.github.rexrk.exception.insights.model.ErrorEvent;

public class UiErrorOutputService implements ErrorOutputService {

    @Override
    public void onErrorCaptured(ErrorEvent event) {
        // no-op — UI polls the store directly via REST endpoints
    }

    @Override
    public void onAiExplanationReady(ErrorEvent event) {
        // no-op — AI explanation is already set on the event in the store
    }
}
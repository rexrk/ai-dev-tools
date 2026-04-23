package io.github.rexrk.exception.insights.service.output.ui;

import io.github.rexrk.exception.insights.model.ErrorEvent;
import io.github.rexrk.exception.insights.service.output.ErrorOutput;

public class UiErrorOutput implements ErrorOutput {

    @Override
    public void onErrorCaptured(ErrorEvent event) {
        // no-op — UI polls the store directly via REST endpoints
    }

    @Override
    public void onAiExplanationReady(ErrorEvent event) {
        // no-op — AI explanation is already set on the event in the store
    }
}
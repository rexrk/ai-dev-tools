package io.github.rexrk.exception.insights.service;

import io.github.rexrk.exception.insights.model.AiExplanation;
import io.github.rexrk.exception.insights.model.ErrorEvent;

public class ConsoleErrorOutputService implements ErrorOutputService {

    private static final String BORDER = "═".repeat(62);
    private static final String TOP    = "╔" + BORDER + "╗";
    private static final String BOTTOM = "╚" + BORDER + "╝";
    private static final String ROW    = "║  ";

    @Override
    public void onErrorCaptured(ErrorEvent event) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append(TOP).append("\n");
        sb.append(ROW).append("ERROR INSIGHT").append("\n");
        sb.append(ROW).append("─".repeat(60)).append("\n");
        sb.append(ROW).append("Type      : ").append(event.getType()).append("\n");
        sb.append(ROW).append("Exception : ").append(event.getExceptionClass()).append("\n");
        sb.append(ROW).append("Message   : ").append(event.getMessage()).append("\n");

        if (event.getType() == ErrorEvent.Type.HTTP_REQUEST) {
            sb.append(ROW).append("Request   : ")
              .append(event.getHttpMethod()).append(" ")
              .append(event.getRequestUri()).append("\n");
        }

        event.getContext().forEach((k, v) ->
            sb.append(ROW).append(k).append("       : ").append(v).append("\n"));

        sb.append(ROW).append("─".repeat(60)).append("\n");
        sb.append(ROW).append("Analyzing with AI...").append("\n");
        sb.append(BOTTOM).append("\n");

        System.out.println(sb);
    }

    @Override
    public void onAiExplanationReady(ErrorEvent event) {
        AiExplanation explanation = event.getAiExplanation();
        if (explanation == null) return;

        StringBuilder sb = new StringBuilder("\n");
        sb.append(TOP).append("\n");
        sb.append(ROW).append("ERROR INSIGHT — AI ANALYSIS").append("\n");
        sb.append(ROW).append("─".repeat(60)).append("\n");

        sb.append(ROW).append("Summary :").append("\n");
        sb.append(ROW).append("  ").append(explanation.summary()).append("\n");

        sb.append(ROW).append("─".repeat(60)).append("\n");
        sb.append(ROW).append("Causes :").append("\n");
        explanation.causes().forEach(c ->
            sb.append(ROW).append("  • ").append(c).append("\n"));

        sb.append(ROW).append("─".repeat(60)).append("\n");
        sb.append(ROW).append("Fixes :").append("\n");
        explanation.fixes().forEach(f ->
            sb.append(ROW).append("  • ").append(f).append("\n"));

        sb.append(BOTTOM).append("\n");

        System.out.println(sb);
    }
}
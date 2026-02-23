package io.github.rexrk.exception.insights.model;

import java.util.List;

public record AiExplanation(
        String summary,
        List<String> causes,
        List<String> fixes,
        String rawResponse
) {}
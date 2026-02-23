package io.github.rexrk.exception.insights.model;

import java.util.List;

public record AiPromptContext(

    // One-line summary
    String exceptionClass,
    String exceptionMessage,

    // Trimmed stack (top frames only)
    List<String> topStackFrames,

    // Last few meaningful logs (already formatted)
    List<String> recentLogMessages,

    // What kind of execution this was
    String executionContext   // e.g. "HTTP POST /users", "Scheduled task: cleanupJob"
) {}
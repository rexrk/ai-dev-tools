package io.github.rexrk.exception.insights.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ErrorEvent {

    public enum Type {
        HTTP_REQUEST,
        ASYNC,
        SCHEDULED,
        TRANSACTIONAL,
        EVENT_LISTENER,
        UNCAUGHT_THREAD
    }

    // --- Identity ---
    private final String id;
    private final Instant timestamp;
    private final Type type;

    // --- Exception details ---
    private final String exceptionClass;
    private final String message;
    private final String stackTrace;
    private final String rootCauseClass;
    private final String rootCauseMessage;
    private final String fingerprint;

    // --- HTTP context (only populated for HTTP_REQUEST type) ---
    private final String httpMethod;
    private final String requestUri;
    private final Map<String, String> requestHeaders;
    private final String requestBody;

    // --- General context (thread name, method name, etc.) ---
    private final Map<String, String> context;

    // --- Log lines captured just before this error occurred ---
    private final List<LogLine> recentLogs;

    // --- Mutable: set asynchronously after AI call completes ---
    private volatile AiExplanation aiExplanation;

    private ErrorEvent(Builder builder) {
        this.id          = UUID.randomUUID().toString();
        this.timestamp   = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.type        = builder.type;

        this.exceptionClass    = builder.exceptionClass;
        this.message           = builder.message;
        this.stackTrace        = builder.stackTrace;
        this.rootCauseClass    = builder.rootCauseClass;
        this.rootCauseMessage  = builder.rootCauseMessage;
        this.fingerprint       = builder.fingerprint;

        this.httpMethod      = builder.httpMethod;
        this.requestUri      = builder.requestUri;
        this.requestHeaders  = builder.requestHeaders.isEmpty()
            ? Collections.emptyMap()
            : Map.copyOf(builder.requestHeaders);
        this.requestBody     = builder.requestBody;

        this.context     = builder.context.isEmpty()
            ? Collections.emptyMap()
            : Map.copyOf(builder.context);
        this.recentLogs  = builder.recentLogs.isEmpty()
            ? Collections.emptyList()
            : Collections.unmodifiableList(builder.recentLogs);

        this.aiExplanation = null;
    }

    // The only setter — called by AiExplanationService when the async call returns
    public void setAiExplanation(AiExplanation aiExplanation) {
        this.aiExplanation = aiExplanation;
    }

    public static Builder builder() {
        return new Builder();
    }

    // -------------------------------------------------------------------------

    public static final class Builder {

        private Instant timestamp;
        private Type type;

        private String exceptionClass;
        private String message;
        private String stackTrace;
        private String rootCauseClass;
        private String rootCauseMessage;
        private String fingerprint;

        private String httpMethod;
        private String requestUri;
        private final Map<String, String> requestHeaders = new HashMap<>();
        private String requestBody;

        private final Map<String, String> context = new HashMap<>();
        private List<LogLine> recentLogs = Collections.emptyList();

        private Builder() {}

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Extracts everything from the throwable in one call:
         * class, message, root cause, stack trace string, and fingerprint.
         */
        public Builder exception(Throwable ex) {
            this.exceptionClass = ex.getClass().getName();
            this.message        = ex.getMessage();
            this.stackTrace     = buildStackTraceString(ex);
            this.fingerprint    = buildFingerprint(ex);

            Throwable root = ex;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            this.rootCauseClass   = root.getClass().getName();
            this.rootCauseMessage = root.getMessage();

            return this;
        }

        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder requestUri(String requestUri) {
            this.requestUri = requestUri;
            return this;
        }

        public Builder requestHeaders(Map<String, String> headers) {
            this.requestHeaders.putAll(headers);
            return this;
        }

        public Builder requestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder context(String key, String value) {
            this.context.put(key, value);
            return this;
        }

        public Builder recentLogs(List<LogLine> recentLogs) {
            this.recentLogs = recentLogs;
            return this;
        }

        public ErrorEvent build() {
            if (type == null) {
                throw new IllegalStateException("ErrorEvent.Type is required");
            }
            if (exceptionClass == null) {
                throw new IllegalStateException("Call .exception(Throwable) before .build()");
            }
            return new ErrorEvent(this);
        }

        // --- Private helpers ---

        private String buildStackTraceString(Throwable ex) {
            StringBuilder sb = new StringBuilder();
            sb.append(ex).append("\n");
            for (StackTraceElement frame : ex.getStackTrace()) {
                sb.append("\tat ").append(frame).append("\n");
            }
            Throwable cause = ex.getCause();
            if (cause != null) {
                sb.append("Caused by: ").append(cause).append("\n");
                for (StackTraceElement frame : cause.getStackTrace()) {
                    sb.append("\tat ").append(frame).append("\n");
                }
            }
            return sb.toString();
        }

        private String buildFingerprint(Throwable ex) {
            StringBuilder sb = new StringBuilder(ex.getClass().getName());
            StackTraceElement[] frames = ex.getStackTrace();
            int limit = Math.min(3, frames.length);
            for (int i = 0; i < limit; i++) {
                sb.append("|")
                  .append(frames[i].getClassName())
                  .append(".")
                  .append(frames[i].getMethodName())
                  .append(":")
                  .append(frames[i].getLineNumber());
            }
            // hex string, short and stable
            return Integer.toHexString(sb.toString().hashCode());
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getId()                           { return id; }
    public Instant getTimestamp()                   { return timestamp; }
    public Type getType()                           { return type; }
    public String getExceptionClass()               { return exceptionClass; }
    public String getMessage()                      { return message; }
    public String getStackTrace()                   { return stackTrace; }
    public String getRootCauseClass()               { return rootCauseClass; }
    public String getRootCauseMessage()             { return rootCauseMessage; }
    public String getFingerprint()                  { return fingerprint; }
    public String getHttpMethod()                   { return httpMethod; }
    public String getRequestUri()                   { return requestUri; }
    public Map<String, String> getRequestHeaders()  { return requestHeaders; }
    public String getRequestBody()                  { return requestBody; }
    public Map<String, String> getContext()         { return context; }
    public List<LogLine> getRecentLogs()            { return recentLogs; }
    public AiExplanation getAiExplanation()         { return aiExplanation; }
}
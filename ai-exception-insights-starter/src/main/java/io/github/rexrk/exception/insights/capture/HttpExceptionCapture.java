package io.github.rexrk.exception.insights.capture;

import io.github.rexrk.exception.insights.model.ErrorEvent;
import io.github.rexrk.exception.insights.service.ai.AiExplanationService;
import io.github.rexrk.exception.insights.store.InMemoryErrorEventStore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class HttpExceptionCapture {

    private final InMemoryErrorEventStore store;
    private final AiExplanationService aiService;
    private final RingBufferLogAppender logAppender;

    public HttpExceptionCapture(InMemoryErrorEventStore store,
                                RingBufferLogAppender logAppender,
                                AiExplanationService aiService
    ) {
        this.store = store;
        this.aiService = aiService;
        this.logAppender = logAppender;
    }

    @ExceptionHandler(Exception.class)
    public void handleAll(Exception ex, HttpServletRequest request) throws Exception {
        ErrorEvent event = ErrorEvent.builder()
                .type(ErrorEvent.Type.HTTP_REQUEST)
                .exception(ex)
                .httpMethod(request.getMethod())
                .requestUri(request.getRequestURI())
                .requestHeaders(extractHeaders(request))
                .requestBody(extractBody(request))
                .recentLogs(logAppender.drainRecent(5))
                .build();

        store.save(event);
        aiService.explainAsync(event);
        throw ex;
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!name.equalsIgnoreCase("authorization") &&
                    !name.equalsIgnoreCase("cookie")) {
                headers.put(name, request.getHeader(name));
            }
        }
        return headers;
    }

    private String extractBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] body = wrapper.getContentAsByteArray();
            return body.length > 0 ? new String(body) : null;
        }
        return null;
    }
}
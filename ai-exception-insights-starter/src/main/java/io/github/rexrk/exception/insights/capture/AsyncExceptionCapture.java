package io.github.rexrk.exception.insights.capture;

import io.github.rexrk.exception.insights.model.ErrorEvent;
import io.github.rexrk.exception.insights.store.InMemoryErrorEventStore;
import org.jspecify.annotations.NonNull;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;
import java.util.Arrays;

public class AsyncExceptionCapture implements AsyncUncaughtExceptionHandler {

    private final InMemoryErrorEventStore store;
    private final RingBufferLogAppender logAppender;

    public AsyncExceptionCapture(InMemoryErrorEventStore store,
                                 RingBufferLogAppender logAppender) {
        this.store = store;
        this.logAppender = logAppender;
    }

    @Override
    public void handleUncaughtException(@NonNull Throwable ex, Method method, Object @NonNull ... params) {
        ErrorEvent event = ErrorEvent.builder()
            .type(ErrorEvent.Type.ASYNC)
            .exception(ex)
            .context("class", method.getDeclaringClass().getSimpleName())
            .context("method", method.getName())
            .context("params", Arrays.toString(params))
            .context("thread", Thread.currentThread().getName())
            .recentLogs(logAppender.drainRecent(10))
            .build();

        store.save(event);
        // aiService.explainAsync(event); -- wire after AI service is built
    }
}
package io.github.rexrk.exception.insights.capture;

import io.github.rexrk.exception.insights.model.ErrorEvent;
import io.github.rexrk.exception.insights.model.ErrorEvent.Type;
import io.github.rexrk.exception.insights.store.InMemoryErrorEventStore;
import org.springframework.beans.factory.InitializingBean;

public class UncaughtThreadExceptionCapture implements InitializingBean {

    private final InMemoryErrorEventStore store;
    private final RingBufferLogAppender logAppender;

    public UncaughtThreadExceptionCapture(InMemoryErrorEventStore store,
                                          RingBufferLogAppender logAppender) {
        this.store = store;
        this.logAppender = logAppender;
    }

    @Override
    public void afterPropertiesSet() {
        Thread.UncaughtExceptionHandler existing =
                Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            try {
                ErrorEvent event = ErrorEvent.builder()
                        .type(Type.UNCAUGHT_THREAD)
                        .exception(ex)
                        .context("thread", thread.getName())
                        .context("threadGroup", thread.getThreadGroup().getName())
                        .recentLogs(logAppender.drainRecent(10))
                        .build();

                store.save(event);
                // aiService.explainAsync(event); -- wire after AI service is built

            } catch (Exception captureFailure) {
                // never let capture logic break JVM exception handling
            } finally {
                // always delegate to the original handler
                if (existing != null) {
                    existing.uncaughtException(thread, ex);
                }
            }
        });
    }
}
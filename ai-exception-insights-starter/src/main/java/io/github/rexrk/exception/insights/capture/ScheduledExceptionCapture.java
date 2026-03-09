package io.github.rexrk.exception.insights.capture;

import io.github.rexrk.exception.insights.model.ErrorEvent;
import io.github.rexrk.exception.insights.service.AiExplanationService;
import io.github.rexrk.exception.insights.store.InMemoryErrorEventStore;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class ScheduledExceptionCapture implements BeanPostProcessor {

    private final InMemoryErrorEventStore store;
    private final RingBufferLogAppender logAppender;
    private final AiExplanationService aiService;

    public ScheduledExceptionCapture(InMemoryErrorEventStore store,
                                     RingBufferLogAppender logAppender,
                                     AiExplanationService aiService) {
        this.store = store;
        this.logAppender = logAppender;
        this.aiService = aiService;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName)
            throws BeansException {

        if (bean instanceof ThreadPoolTaskScheduler scheduler) {
            scheduler.setErrorHandler(this::capture);
        }
        return bean;
    }

    private void capture(Throwable ex) {
        ErrorEvent event = ErrorEvent.builder()
                .type(ErrorEvent.Type.SCHEDULED)
                .exception(ex)
                .context("thread", Thread.currentThread().getName())
                .recentLogs(logAppender.drainRecent(10))
                .build();

        store.save(event);
        aiService.explainAsync(event);
    }
}
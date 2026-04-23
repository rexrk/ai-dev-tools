package io.github.rexrk.exception.insights.capture;

import io.github.rexrk.exception.insights.model.ErrorEvent;
import io.github.rexrk.exception.insights.service.ai.AiExplanationService;
import io.github.rexrk.exception.insights.store.InMemoryErrorEventStore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.RequestContextHolder;

@Aspect
public class AopExceptionCapture {

    private final InMemoryErrorEventStore store;
    private final RingBufferLogAppender logAppender;
    private final AiExplanationService aiService;

    public AopExceptionCapture(InMemoryErrorEventStore store,
                               RingBufferLogAppender logAppender,
                               AiExplanationService aiService) {
        this.store = store;
        this.logAppender = logAppender;
        this.aiService = aiService;
    }

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object captureTransactional(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Exception ex) {
            // skip if inside a web request — HttpExceptionCapture handles it there
            if (RequestContextHolder.getRequestAttributes() == null) {
                capture(ex, ErrorEvent.Type.TRANSACTIONAL, pjp.getSignature().toShortString());
            }
            throw ex;
        }
    }

    @Around("@annotation(org.springframework.context.event.EventListener)" +
            " || @annotation(org.springframework.context.event.TransactionalEventListener)")
    public Object captureEventListener(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Exception ex) {
            capture(ex, ErrorEvent.Type.EVENT_LISTENER, pjp.getSignature().toShortString());
            throw ex;
        }
    }

    private void capture(Exception ex, ErrorEvent.Type type, String signature) {
        ErrorEvent event = ErrorEvent.builder()
                .type(type)
                .exception(ex)
                .context("method", signature)
                .context("thread", Thread.currentThread().getName())
                .recentLogs(logAppender.drainRecent(10))
                .build();

        store.save(event);
        aiService.explainAsync(event);
    }
}
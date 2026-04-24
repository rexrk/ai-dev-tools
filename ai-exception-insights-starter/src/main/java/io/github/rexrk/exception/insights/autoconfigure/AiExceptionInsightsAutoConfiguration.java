package io.github.rexrk.exception.insights.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rexrk.exception.insights.capture.*;
import io.github.rexrk.exception.insights.service.ai.AiExplanationService;
import io.github.rexrk.exception.insights.service.output.console.ConsoleErrorOutput;
import io.github.rexrk.exception.insights.service.output.ErrorOutput;
import io.github.rexrk.exception.insights.service.output.ui.UiErrorOutput;
import io.github.rexrk.exception.insights.store.InMemoryErrorEventStore;
import io.github.rexrk.ui.dashboard.SseEmitterRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@AutoConfiguration
@EnableAsync
@ConditionalOnProperty(
        prefix = AiExceptionInsightProperties.prefix,
        name = "enabled",
        matchIfMissing = true
)
@EnableConfigurationProperties(AiExceptionInsightProperties.class)
public class AiExceptionInsightsAutoConfiguration {

    // --- Output Service ---

    @Bean
    @ConditionalOnProperty(prefix = AiExceptionInsightProperties.prefix,
            name = "output",
            havingValue = "CONSOLE",
            matchIfMissing = true)
    public ErrorOutput consoleErrorOutputService() {
        return new ConsoleErrorOutput();
    }

    @Bean
    @ConditionalOnProperty(prefix = AiExceptionInsightProperties.prefix,
            name = "output",
            havingValue = "UI")
    public ErrorOutput uiErrorOutputService(SseEmitterRegistry emitterRegistry) {
        return new UiErrorOutput(emitterRegistry);
    }

    // --- Store ---

    @Bean
    public InMemoryErrorEventStore errorEventStore(
            AiExceptionInsightProperties props,
            ErrorOutput outputService
    ) {
        return new InMemoryErrorEventStore(
                props.getMaxEvents(),
                props.getDeduplicationWindow(),
                outputService
        );
    }

    // --- Log Appender ---

    @Bean
    public RingBufferLogAppender ringBufferLogAppender(AiExceptionInsightProperties props) {
        return new RingBufferLogAppender(props.getLogBufferSize());
    }

    @Bean
    public LogAppenderRegistrar logAppenderRegistrar(RingBufferLogAppender appender) {
        return new LogAppenderRegistrar(appender);
    }

    // --- AI Service ---

    @Bean
    @ConditionalOnMissingBean
    public AiExplanationService aiExplanationService(ChatClient.Builder builder,
                                                     ObjectMapper  objectMapper,
                                                     ErrorOutput outputService) {
        return new AiExplanationService(builder.build(),objectMapper, outputService);
    }

    // --- Capture Mechanisms ---

    @Bean
    @ConditionalOnWebApplication
    public HttpExceptionCapture httpExceptionCapture(InMemoryErrorEventStore store,
                                                     RingBufferLogAppender logAppender,
                                                     AiExplanationService aiService) {
        return new HttpExceptionCapture(store, logAppender, aiService);
    }

    @Bean
    @ConditionalOnWebApplication
    public RequestCachingFilter requestCachingFilter() {
        return new RequestCachingFilter();
    }

    @Bean
    public AsyncExceptionCapture asyncExceptionCapture(InMemoryErrorEventStore store,
                                                       AiExplanationService aiService,
                                                       RingBufferLogAppender logAppender) {
        return new AsyncExceptionCapture(store, logAppender, aiService);
    }

    @Bean
    @ConditionalOnMissingBean(org.springframework.util.ErrorHandler.class)
    public ScheduledExceptionCapture scheduledExceptionCapture(
            InMemoryErrorEventStore store,
            RingBufferLogAppender logAppender,
            AiExplanationService aiService
    ) {
        return new ScheduledExceptionCapture(store, logAppender, aiService);
    }

    @Bean
    @ConditionalOnMissingBean
    public UncaughtThreadExceptionCapture uncaughtThreadExceptionCapture(
            InMemoryErrorEventStore store,
            RingBufferLogAppender logAppender,
            AiExplanationService aiService) {
        return new UncaughtThreadExceptionCapture(store, logAppender, aiService);
    }

    // --- Async Config ---

    @Bean
    @ConditionalOnMissingBean
    public AsyncConfigurer asyncConfigurer(AsyncExceptionCapture asyncCapture) {
        return new AsyncConfigurer() {
            @Override
            public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
                return asyncCapture;
            }
        };
    }

}

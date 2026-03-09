package io.github.rexrk.exception.insights.autoconfigure;

import io.github.rexrk.exception.insights.capture.RingBufferLogAppender;
import io.github.rexrk.exception.insights.capture.ScheduledExceptionCapture;
import io.github.rexrk.exception.insights.store.InMemoryErrorEventStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(
        prefix = AiExceptionInsightProperties.prefix,
        name = "enabled",
        matchIfMissing = true
)
@EnableConfigurationProperties(AiExceptionInsightProperties.class)
public class AiExceptionInsightsAutoConfiguration {

    @Bean
    public InMemoryErrorEventStore errorEventStore(
            AiExceptionInsightProperties props
    ) {
        return new InMemoryErrorEventStore(
                props.getMaxEvents(),
                props.getDeduplicationWindow()
        );
    }

    @Bean
    @ConditionalOnMissingBean(org.springframework.util.ErrorHandler.class)
    public ScheduledExceptionCapture scheduledExceptionCapture(
            InMemoryErrorEventStore store,
            RingBufferLogAppender logAppender
    ) {
        return new ScheduledExceptionCapture(store, logAppender);
    }

}

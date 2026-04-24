package io.github.rexrk.ui.dashboard;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class DevToolsUiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SseEmitterRegistry sseEmitterRegistry() {
        return new SseEmitterRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public DashboardController dashboardController(SseEmitterRegistry registry) {
        return new DashboardController(registry);
    }
}
package io.github.rexrk.ui.dashboard;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "io.github.rexrk.ui.dashboard")
@ConditionalOnProperty(prefix = "devtools.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DevToolsUiAutoConfiguration {
}
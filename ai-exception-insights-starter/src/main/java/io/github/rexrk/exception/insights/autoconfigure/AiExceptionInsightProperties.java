package io.github.rexrk.exception.insights.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(AiExceptionInsightProperties.prefix)
public class AiExceptionInsightProperties {
    public static final String prefix = "devtools.ai.exception-insights";
    public enum OutputMode { UI, CONSOLE }

    private boolean enabled = true;
    private int maxEvents = 10;
    private Duration deduplicationWindow = Duration.ofSeconds(5);
    private int logBufferSize = 5;
    private OutputMode output = OutputMode.UI;

    // getters and setters
    public boolean isEnabled()                       { return enabled; }
    public void setEnabled(boolean enabled)          { this.enabled = enabled; }
    public int getMaxEvents()                        { return maxEvents; }
    public void setMaxEvents(int maxEvents)          { this.maxEvents = maxEvents; }
    public int getLogBufferSize()                    { return logBufferSize; }
    public void setLogBufferSize(int logBufferSize)  { this.logBufferSize = logBufferSize; }
    public Duration getDeduplicationWindow()         { return deduplicationWindow; }
    public void setDeduplicationWindow(Duration d)   { this.deduplicationWindow = d; }
    public OutputMode getOutput()                    { return output; }
    public void setOutput(OutputMode output)         { this.output = output; }

}
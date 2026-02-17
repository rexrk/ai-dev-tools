package io.github.rexrk;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = AiSwaggerHelperProperties.prefix)
public class AiSwaggerHelperProperties {
    public static final String prefix = "devtools.ai.swagger";
    private boolean enabled = true;
    private Mode mode = Mode.AUTO;

    public enum Mode {
        AI,
        RANDOM,
        AUTO
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }
}
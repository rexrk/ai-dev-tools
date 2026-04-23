package io.github.rexrk.demoapp;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BoomEventListener {

    @EventListener
    public void handle(BoomEvent event) {
        throw new RuntimeException("Event listener boom");
    }
}
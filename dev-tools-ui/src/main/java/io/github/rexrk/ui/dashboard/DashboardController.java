package io.github.rexrk.ui.dashboard;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/dev-tools")
public class DashboardController {

    private final SseEmitterRegistry registry;

    public DashboardController(SseEmitterRegistry registry) {
        this.registry = registry;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return registry.register(UUID.randomUUID().toString());
    }

}
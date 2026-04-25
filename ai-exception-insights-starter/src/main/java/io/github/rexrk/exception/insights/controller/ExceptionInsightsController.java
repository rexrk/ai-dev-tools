package io.github.rexrk.exception.insights.controller;

import io.github.rexrk.exception.insights.model.ErrorEvent;
import io.github.rexrk.exception.insights.store.InMemoryErrorEventStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exception-insights")
public class ExceptionInsightsController {

    private final InMemoryErrorEventStore store;

    public ExceptionInsightsController(InMemoryErrorEventStore store) {
        this.store = store;
    }

    @GetMapping("/events")
    public List<ErrorEvent> getEvents(@RequestParam(defaultValue = "20", name = "limit") int limit) {
        return store.getRecent(limit);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<ErrorEvent> getEvent(@PathVariable("id") String id) {
        return store.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/events")
    public ResponseEntity<Void> clearAll() {
        store.clear();
        return ResponseEntity.noContent().build();
    }
}
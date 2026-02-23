package io.github.rexrk.exception.insights.store;

import io.github.rexrk.exception.insights.model.ErrorEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class InMemoryErrorEventStore {

    private final Deque<ErrorEvent> events;
    private final int maxEvents;
    private final Duration deduplicationWindow;

    public InMemoryErrorEventStore(int maxEvents, Duration deduplicationWindow) {
        this.maxEvents = maxEvents;
        this.deduplicationWindow = deduplicationWindow;
        this.events = new ArrayDeque<>(maxEvents);
    }

    public synchronized void save(ErrorEvent event) {
        if (event.getFingerprint() != null && isDuplicate(event)) {
            return;
        }
        if (events.size() == maxEvents) {
            events.pollFirst(); // drop oldest
        }
        events.addLast(event);
    }

    public synchronized List<ErrorEvent> getRecent(int limit) {
        return events.reversed().stream()
                .limit(limit)
                .toList();
    }

    public synchronized Optional<ErrorEvent> findById(String id) {
        return events.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }

    public synchronized void clear() {
        events.clear();
    }

    public synchronized int size() {
        return events.size();
    }

    private boolean isDuplicate(ErrorEvent incoming) {
        Instant windowStart = Instant.now().minus(deduplicationWindow);
        return events.stream().anyMatch(existing ->
                incoming.getFingerprint().equals(existing.getFingerprint())
                        && existing.getTimestamp().isAfter(windowStart)
        );
    }
}
package io.github.rexrk.exception.insights.capture;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import io.github.rexrk.exception.insights.model.LogLine;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class RingBufferLogAppender extends AppenderBase<ILoggingEvent> {

    private final int bufferSize;
    private final Deque<LogLine> buffer;

    public RingBufferLogAppender(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new ArrayDeque<>(bufferSize);
    }

    @Override
    protected synchronized void append(ILoggingEvent event) {
        if (!event.getLevel().isGreaterOrEqual(Level.WARN)) return;

        if (buffer.size() == bufferSize) {
            buffer.pollFirst();
        }

        buffer.addLast(new LogLine(
            event.getLevel().toString(),
            event.getFormattedMessage(),
            event.getLoggerName(),
            event.getThreadName(),
            throwableClass(event.getThrowableProxy()),
            throwableMessage(event.getThrowableProxy()),
            Instant.ofEpochMilli(event.getTimeStamp())
        ));
    }

    public synchronized List<LogLine> drainRecent(int limit) {
        List<LogLine> all = new ArrayList<>(buffer);
        int from = Math.max(0, all.size() - limit);
        return all.subList(from, all.size());
    }

    private String throwableClass(IThrowableProxy proxy) {
        return proxy != null ? proxy.getClassName() : null;
    }

    private String throwableMessage(IThrowableProxy proxy) {
        return proxy != null ? proxy.getMessage() : null;
    }
}
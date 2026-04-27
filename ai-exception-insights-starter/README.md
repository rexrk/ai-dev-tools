# ai-exception-insights-starter

`ai-exception-insights-starter` captures runtime failures, stores recent error events in memory, and asks a Spring AI chat model for a short explanation with suggested fixes.

## What It Captures

- Unhandled HTTP exceptions in web applications
- `@Async` uncaught exceptions
- Scheduled-task exceptions when the host app enables scheduling
- Uncaught thread exceptions
- Recent `WARN` and `ERROR` log lines from a small in-memory ring buffer

Each captured event includes exception details, a short fingerprint for deduplication, recent logs, and an asynchronously populated AI explanation.

## Dependency

```xml
<dependency>
    <groupId>io.github.rexrk</groupId>
    <artifactId>ai-exception-insights-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration

| Property | Default | Description |
| --- | --- | --- |
| `devtools.ai.exception-insights.enabled` | `true` | Enables the starter auto-configuration. |
| `devtools.ai.exception-insights.max-events` | `10` | Maximum number of events retained in memory. |
| `devtools.ai.exception-insights.deduplication-window` | `5s` | Drops duplicate fingerprints seen inside this window. |
| `devtools.ai.exception-insights.log-buffer-size` | `5` | Number of recent `WARN`/`ERROR` log lines retained. |
| `devtools.ai.exception-insights.output` | `CONSOLE` | Output mode: `CONSOLE` or `UI`. |

Example:

```yaml
devtools:
  ai:
    exception-insights:
      enabled: true
      max-events: 25
      deduplication-window: 10s
      log-buffer-size: 20
      output: ui
```

Spring AI example:

```yaml
spring:
  ai:
    openai:
      api-key: ${API_KEY}
```

## Runtime Endpoints

The starter exposes a small inspection API:

- `GET /exception-insights/events?limit=20`
- `GET /exception-insights/events/{id}`
- `DELETE /exception-insights/events`

## Output Modes

### `CONSOLE`

Logs a formatted error block when an event is captured and logs a second block when the AI explanation is ready.

### `UI`

Uses the bundled `dev-tools-ui` dependency to broadcast SSE events and render the dashboard in the browser. In this mode:

- the dashboard is available from the app's root static page
- SSE is exposed from `/dev-tools/stream`
- the exception API continues to serve full event details

## Behavior Notes

- Storage is in-memory only; restarting the app clears history.
- HTTP request capture excludes `Authorization` and `Cookie` headers from the stored request-header map.
- Scheduled-task capture only activates in applications that already enable scheduling.
- If the AI call fails, the event is still stored and the explanation is replaced with a fallback message.

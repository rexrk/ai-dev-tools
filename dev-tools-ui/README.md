# dev-tools-ui

`dev-tools-ui` is a reusable dashboard module for development-time event streaming. It ships static browser assets plus a simple SSE registry that other modules can publish to.

## What It Provides

- `SseEmitterRegistry` bean for broadcasting named SSE events
- `GET /dev-tools/stream` SSE endpoint
- Static dashboard assets:
  - `/`
  - `/dashboard.js`
  - `/dashboard.css`

## Primary Use Case

This module is currently used by `ai-exception-insights-starter` when `devtools.ai.exception-insights.output=UI`. The exception starter publishes two event types through the registry:

- `error-captured`
- `ai-insight-ready`

## Dashboard Contract

The bundled frontend is not a generic event inspector. It currently expects the following companion API:

- `GET /exception-insights/events`
- `GET /exception-insights/events/{id}`
- `DELETE /exception-insights/events`

If those endpoints are unavailable, the UI falls back to a local demo payload so the page still renders.

## Dependency

```xml
<dependency>
    <groupId>io.github.rexrk</groupId>
    <artifactId>dev-tools-ui</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Integration Notes

- The module auto-registers `SseEmitterRegistry` and `DashboardController`.
- Any other starter can reuse the registry by injecting `SseEmitterRegistry` and calling `broadcast(eventName, payload)`.
- The current static UI is tailored to exception-insight events, so wider reuse may require replacing the frontend assets or expanding the expected REST contract.

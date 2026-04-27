# demo-app

`demo-app` is the integration sandbox for this repository. It pulls in both starters and the UI module so you can exercise the current developer workflow end to end.

## What It Demonstrates

- Swagger request-body generation on `POST /test/generate`
- Browser dashboard output from `ai-exception-insights-starter`
- HTTP exception capture
- `@Async` exception capture
- Uncaught-thread exception capture

The app also includes an event-listener failure example. Because that listener is triggered from an HTTP request, it will surface through the HTTP exception capture path.

## Configuration

The sample app is configured to use Spring AI's OpenAI adapter against an OpenAI-compatible endpoint:

```yaml
spring:
  ai:
    openai:
      api-key: ${API_KEY}
      chat:
        base-url: https://openrouter.ai/api
        options:
          model: inclusionai/ling-2.6-1t:free

devtools:
  ai:
    swagger-helper:
      enabled: true
      mode: ai
    exception-insights:
      enabled: true
      output: ui

server:
  port: 8081
```

## Run

```powershell
$env:API_KEY="your-api-key"
.\mvnw.cmd -pl demo-app -am spring-boot:run
```

## Try It

- Open `http://localhost:8081/swagger-ui/index.html`
- Call `POST /test/generate` and use the generated-body action in Swagger UI
- Open `http://localhost:8081/` to watch the exception dashboard
- Trigger failures:
  - `GET /boom/http`
  - `GET /boom/async`
  - `GET /boom/thread`
  - `GET /boom/event`

## Notes

- `@Async` support is active because the exception starter enables async processing.
- The codebase includes a scheduled exception example, but this demo app does not enable scheduling, so that path is inactive unless you add `@EnableScheduling`.

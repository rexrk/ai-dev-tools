# AI Dev Tools for Spring Boot

`ai-dev-tools` is a multi-module Maven workspace for Spring Boot developer tooling. The modules in this repository focus on local development workflows such as Swagger payload generation, exception capture, and live debugging dashboards.

## Modules

| Module | Purpose |
| --- | --- |
| `ai-swagger-helper-starter` | Injects a Swagger UI plugin that can generate JSON request bodies from OpenAPI schemas. |
| `ai-exception-insights-starter` | Captures runtime failures, stores recent error events, and asks a Spring AI chat model for a short diagnosis. |
| `dev-tools-ui` | Reusable SSE dashboard module used by the exception starter for browser-based output. |
| `demo-app` | Sample application that wires the starters together and exposes endpoints to try them. |

Each module has its own README with module-specific setup and usage details.

## Stack

- Java 21
- Spring Boot 4.0.2
- Spring AI 2.0.0-M2
- Maven multi-module build
- `springdoc-openapi` for Swagger UI integration

## Prerequisites

- JDK 21
- Maven 3.9+ or the included wrapper
- An OpenAI-compatible API key if you want live AI responses

The repository already includes sample configuration for an OpenAI-compatible endpoint through Spring AI. The demo app uses OpenRouter through the OpenAI adapter.

## Build

Unix-like shells:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## Run the Demo App

Set an API key first if you want AI-backed generation and exception analysis:

```powershell
$env:API_KEY="your-api-key"
.\mvnw.cmd -pl demo-app -am spring-boot:run
```

Useful URLs after startup:

- Dashboard: `http://localhost:8081/`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- Exception event API: `http://localhost:8081/exception-insights/events`

Demo endpoints:

- `POST /test/generate` for Swagger request-body generation
- `GET /boom/http` for HTTP exception capture
- `GET /boom/async` for `@Async` exception capture
- `GET /boom/thread` for uncaught thread exception capture
- `GET /boom/event` for an event-listener failure triggered from an HTTP request

## Configuration Example

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
```

## Notes

- `ai-swagger-helper-starter` supports `AUTO`, `AI`, and `RANDOM` modes.
- `ai-exception-insights-starter` stores events in memory only and can output to the console or the browser UI.
- `dev-tools-ui` is reusable, but the shipped dashboard is currently tailored to the exception starter's REST and SSE contract.

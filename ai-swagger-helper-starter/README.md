# ai-swagger-helper-starter

`ai-swagger-helper-starter` adds a small Swagger UI extension plus a backend generator endpoint so developers can fill JSON request bodies without hand-writing payloads during local API testing.

## What the Starter Adds

- A Swagger UI plugin injected into springdoc's `index.html`
- A "generate body" action inside the request-body editor
- `POST /devtools/ai/request-body/generate`
- Three generation modes:
  - `RANDOM`: local schema-driven sample generation
  - `AI`: Spring AI chat-model generation
  - `AUTO`: AI when available, otherwise local generation

When AI generation fails or returns invalid JSON, the starter falls back to the local generator.

## Requirements

- Spring MVC application
- `springdoc-openapi-starter-webmvc-ui` on the host application classpath
- Spring AI model configuration if you want `AI` or `AUTO` mode to use a real chat model

## Dependency

```xml
<dependency>
    <groupId>io.github.rexrk</groupId>
    <artifactId>ai-swagger-helper-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration

| Property | Default | Description |
| --- | --- | --- |
| `devtools.ai.swagger-helper.enabled` | `true` | Enables the starter auto-configuration. |
| `devtools.ai.swagger-helper.mode` | `AUTO` | Generation mode: `AUTO`, `AI`, or `RANDOM`. |

Example:

```yaml
devtools:
  ai:
    swagger-helper:
      enabled: true
      mode: auto
```

Spring AI example with the OpenAI adapter:

```yaml
spring:
  ai:
    openai:
      api-key: ${API_KEY}
```

## How It Works

1. The frontend plugin resolves `$ref` entries from the loaded OpenAPI document.
2. It posts a resolved schema to `/devtools/ai/request-body/generate`.
3. The backend returns a JSON string.
4. Swagger UI writes that JSON back into the request-body editor.

The local generator supports:

- objects and nested objects
- arrays
- enums
- primitive types
- string formats such as `date`, `date-time`, `email`, `uuid`, and `uri`

## Usage

1. Start an application that includes this starter and springdoc Swagger UI.
2. Open Swagger UI.
3. Expand an operation that accepts a JSON request body.
4. Use the generated-body action in the request editor.

## Current Limitation

The current frontend plugin scans the loaded OpenAPI document and uses the first JSON request-body schema it finds. In specs with multiple request-body operations, generation may not always map to the currently selected operation.

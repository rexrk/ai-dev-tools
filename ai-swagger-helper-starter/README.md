# DevTools AI – Swagger Helper Starter

A Spring Boot starter that improves **Swagger / OpenAPI developer experience** by automatically generating request bodies during development.

The starter supports both **local random generation** and **optional AI-powered generation** (via Spring AI), with safe fallbacks and zero impact on application startup.

AI is treated as an **optional enhancement**, not a requirement.

---

## 🎯 Problem

During backend development, generating request bodies for APIs is:

- repetitive  
- time-consuming  
- error-prone  
- especially painful for complex schemas  

Swagger UI shows schemas, but developers still have to manually craft payloads.

This starter removes that friction.

---

## ✨ Features

- Swagger UI extension with **Generate Request Body** action  
- Local random request body generation (default)  
- Optional AI-powered request body generation  
- Automatic fallback to random generation on AI failure  
- Fully configurable via properties

---

## 📦 Installation

### Maven

```xml
<dependency>
    <groupId>io.github.rexrk</groupId>
    <artifactId>ai-swagger-helper-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

---

## ⚙️ Configuration

Configure Spring AI (example with OpenAI):
```yaml
spring:
  ai:
    openai:
      api-key: YOUR_API_KEY
```


```yaml
devtools:
  ai:
    swagger:
      enabled: true
      mode: random
```

## 🔁 Generation Modes

| Mode | Behavior |
|-----|----------|
| random | Always use local random generation |
| ai | Use AI if available, fallback to random |
| auto | Use AI when configured, otherwise random |

---
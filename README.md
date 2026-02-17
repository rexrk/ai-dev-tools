# 🚀 AI Dev Tools for Spring Boot

A collection of **AI-powered Spring Boot starters** focused on **enhancing developer experience (DX)** during local development and testing.

This repository contains production-grade tooling ideas that integrate deeply with the Spring ecosystem to help developers:
- Fix APIs faster
- Reduce repetitive work
- Improve development experience during local runs

> 🎯 **Goal**: Build AI-assisted developer tooling, not end-user applications.

---

## 📦 Repository Structure

This is a **multi-module Maven project** with a shared parent POM.
```
ai-dev-tools
├── .github/workflows
├── LICENCE
├── pom.xml # Parent POM
├── ai-swagger-helper-starter # Swagger/OpenAPI DX tools
├── demo-app # Usage example for all starters
└── README.md
```


Each module:
- Is a **Spring Boot starter**
- Is independently usable
- Focuses on a specific developer pain point

---

## 🧠 Why AI + Spring Boot Starters?

Traditional developer tools provide **raw data** (logs, specs, stack traces).  
These starters use AI to convert that data into **actionable insights**.

Examples:
- Explaining *why* an API change is breaking
- Generating realistic request payloads from OpenAPI
- Summarizing errors instead of dumping stack traces
- Reducing cognitive load during development

AI is used **only where reasoning or summarization adds value**, not as a gimmick.

---

## 🧩 Available Starters

### 1️⃣ AI Swagger Helper Starter
📁 `ai-swagger-helper-starter`

Enhancements for Swagger / OpenAPI during development.

**Features**
- Adds a Swagger UI extension
- Generates realistic, real-world request bodies
- Helps developers test APIs faster without manually crafting payloads

**Use case**
- Local development
- API testing

---

## 🛠 Planned / In-Progress Starters

- **AI Exception Explainer Starter**  
  Converts Spring stack traces into human-readable explanations.

- **AI Exception Handler**

(Each starter will live in its own module with independent documentation.)*

---


## 🧰 Tech Stack

- Java 21+
- Spring Boot 4
- Spring Auto-Configuration
- OpenAPI / Swagger
- Maven (multi-module)
- OpenRouter LLM integration (pluggable)

---

## 📌 Who Is This For?

- Backend developers working with Spring Boot
- Engineers interested in platform / tooling work
- Developers exploring AI-assisted development workflows

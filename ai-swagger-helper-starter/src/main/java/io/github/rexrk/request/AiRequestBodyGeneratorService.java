package io.github.rexrk.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.*;

import java.util.*;

public class AiRequestBodyGeneratorService {

    private static final Logger log =
            LoggerFactory.getLogger(AiRequestBodyGeneratorService.class);
    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    public AiRequestBodyGeneratorService(OpenAiChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;

    }

    public String generateBody(JsonNode schema) {
        // If no API key, use random generation
        if (chatModel == null) {
            return generateRandomBody(schema);
        }

        try {
            return generateWithAI(schema);
        } catch (Exception e) {
            // Fallback to random on error
            log.warn("AI generation failed, falling back to random");
            log.debug("Error:",e);
            return generateRandomBody(schema);
        }
    }

    private String generateWithAI(JsonNode schema) throws Exception {
        String schemaStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);

        String promptText = """
            Generate a realistic JSON object that matches this OpenAPI schema.
            Return ONLY valid JSON with no markdown formatting, no explanation.

            Schema:
            %s

            Requirements:
            - All required fields must be present
            - Use realistic, varied sample data (real names, age, emails, addresses, etc.)
            - Follow any format constraints (email, date-time, uuid, etc.)
            - Respect min/max constraints for numbers
            - If there are enums, pick random valid values
            - Make nested objects and arrays realistic
            - Output ONLY the JSON object, nothing else
            """.formatted(schemaStr);


        ChatResponse response = chatModel.call(new Prompt(promptText));
        String generatedText = Optional.of(response)
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .filter(t -> !t.isBlank())
                .orElseThrow(() ->
                        new IllegalStateException("AI response contained no text output"));

        // Clean the response
        String cleaned = extractJson(generatedText);

        log.debug("Cleaned JSON: {}", cleaned);

        // Validate JSON
        try {
            objectMapper.readTree(cleaned);
        } catch (Exception e) {
            log.error("Failed to parse AI response. Raw: {}, Cleaned: {}", generatedText, cleaned);
            throw new RuntimeException("AI returned invalid JSON: " + e.getMessage());
        }

        return cleaned;
    }

    private String extractJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Empty AI response");
        }

        String text = raw.trim();

        // Remove fenced code blocks if present
        if (text.contains("```")) {
            text = text.replaceAll("(?s)```[a-zA-Z]*\\s*(.*?)\\s*```", "$1").trim();
        }

        int objStart = text.indexOf('{');
        int arrStart = text.indexOf('[');

        if (objStart == -1 && arrStart == -1) {
            throw new IllegalArgumentException("No JSON found in AI output");
        }

        int start = (objStart == -1) ? arrStart :
                (arrStart == -1) ? objStart :
                        Math.min(objStart, arrStart);

        String json = text.substring(start);

        int end = Math.max(json.lastIndexOf('}'), json.lastIndexOf(']'));
        if (end == -1) {
            throw new IllegalArgumentException("Incomplete JSON in AI output");
        }

        return json.substring(0, end + 1).trim();
    }


    private String generateRandomBody(JsonNode schema) {
        try {
            Object generated = generateFromSchema(schema);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(generated);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Object generateFromSchema(JsonNode schema) {
        if (schema == null || schema.isNull()) return null;

        // Handle $ref - this won't work without the full spec, so return placeholder
        if (schema.has("$ref")) {
            return Map.of("error", "Schema references not yet resolved. Pass resolved schema.");
        }

        // Handle enum
        if (schema.has("enum")) {
            JsonNode enumNode = schema.get("enum");
            int index = new Random().nextInt(enumNode.size());
            return enumNode.get(index).asText();
        }

        String type = schema.has("type") ? schema.get("type").asText() : "object";

        switch (type.toLowerCase()) {
            case "string":
                String format = schema.has("format") ? schema.get("format").asText() : null;
                return generateRandomString(format);

            case "integer":
                int min = schema.has("minimum") ? schema.get("minimum").asInt() : 1;
                int max = schema.has("maximum") ? schema.get("maximum").asInt() : 1000;
                return min + new Random().nextInt(max - min + 1);

            case "number":
                double minD = schema.has("minimum") ? schema.get("minimum").asDouble() : 1.0;
                double maxD = schema.has("maximum") ? schema.get("maximum").asDouble() : 1000.0;
                return Math.round((minD + Math.random() * (maxD - minD)) * 100.0) / 100.0;

            case "boolean":
                return new Random().nextBoolean();

            case "array":
                int count = schema.has("minItems") ? schema.get("minItems").asInt() : 1;
                JsonNode items = schema.get("items");
                List<Object> array = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    array.add(generateFromSchema(items));
                }
                return array;

            case "object":
            default:
                Map<String, Object> obj = new HashMap<>();

                if (schema.has("properties")) {
                    JsonNode properties = schema.get("properties");

                    properties.properties().forEach(entry -> {
                        obj.put(entry.getKey(), generateFromSchema(entry.getValue()));
                    });
                }
                return obj;
        }
    }

    private String generateRandomString(String format) {
        Random rand = new Random();
        if (format == null) {
            return "string_" + rand.nextInt(1000);
        }

        return switch (format) {
            case "date" -> java.time.LocalDate.now().minusDays(rand.nextInt(365)).toString();
            case "date-time" -> java.time.ZonedDateTime.now().minusDays(rand.nextInt(365)).toString();
            case "email" -> {
                String[] emails = {"alice@example.com", "bob@test.org", "charlie@mail.io"};
                yield emails[rand.nextInt(emails.length)];
            }
            case "uuid" -> UUID.randomUUID().toString();
            case "uri" -> "https://example.com/" + UUID.randomUUID().toString().substring(0, 8);
            default -> "value_" + rand.nextInt(1000);
        };
    }
}
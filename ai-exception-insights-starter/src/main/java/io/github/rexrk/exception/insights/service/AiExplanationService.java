package io.github.rexrk.exception.insights.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rexrk.exception.insights.model.AiExplanation;
import io.github.rexrk.exception.insights.model.AiPromptContext;
import io.github.rexrk.exception.insights.model.ErrorEvent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public class AiExplanationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ErrorOutputService outputService;

    public AiExplanationService(ChatClient chatClient,
                                ObjectMapper objectMapper,
                                ErrorOutputService outputService) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
        this.outputService = outputService;
    }

    @Async
    public void explainAsync(ErrorEvent event) {
        try {
            AiPromptContext context = AiPromptMapper.from(event);
            String prompt = buildPrompt(context);

            String raw = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            AiExplanation explanation = parse(raw);
            event.setAiExplanation(explanation);
            outputService.onAiExplanationReady(event);

        } catch (Exception e) {
            event.setAiExplanation(new AiExplanation(
                    "AI explanation unavailable: " + e.getMessage(),
                    List.of(),
                    List.of(),
                    null
            ));
        }
    }

    private String buildPrompt(AiPromptContext ctx) {
        return """
                You are a Spring Boot expert helping a developer debug a runtime error.
                Respond ONLY in the JSON format shown below. No extra text.
                
                EXECUTION CONTEXT: %s
                
                EXCEPTION: %s
                MESSAGE: %s
                
                STACK TRACE (top frames):
                %s
                
                RECENT LOGS:
                %s
                
                Respond in this exact JSON:
                {
                  "summary": "plain English explanation of what went wrong",
                  "causes": ["cause 1", "cause 2"],
                  "fixes": ["fix 1", "fix 2"]
                }
                """.formatted(
                ctx.executionContext(),
                ctx.exceptionClass(),
                ctx.exceptionMessage(),
                String.join("\n", ctx.topStackFrames()),
                String.join("\n", ctx.recentLogMessages())
        );
    }

    private AiExplanation parse(String raw) {
        try {
            String clean = raw.replaceAll("```json|```", "").trim();
            JsonNode node = objectMapper.readTree(clean);
            return new AiExplanation(
                    node.path("summary").asText(),
                    objectMapper.convertValue(node.path("causes"),
                            objectMapper.getTypeFactory()
                                    .constructCollectionType(List.class, String.class)),
                    objectMapper.convertValue(node.path("fixes"),
                            objectMapper.getTypeFactory()
                                    .constructCollectionType(List.class, String.class)),
                    raw
            );
        } catch (Exception e) {
            return new AiExplanation(
                    "Failed to parse AI response",
                    List.of(),
                    List.of(),
                    raw
            );
        }
    }
}
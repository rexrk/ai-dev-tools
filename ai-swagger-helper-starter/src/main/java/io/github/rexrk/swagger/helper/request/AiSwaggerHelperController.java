package io.github.rexrk.swagger.helper.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AiSwaggerHelperController {

    private static final Logger log =
            LoggerFactory.getLogger(AiSwaggerHelperController.class);

    private final AiRequestBodyGeneratorService generatorService;
    private final ObjectMapper objectMapper;

    public AiSwaggerHelperController(AiRequestBodyGeneratorService generatorService,
                                     ObjectMapper objectMapper) {
        this.generatorService = generatorService;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> generateBody(Map<String, Object> request) {
        Object schemaObj = request.get("schema");
        if (schemaObj == null) {
            throw new IllegalArgumentException("Missing 'schema' in request");
        }

        JsonNode schema = objectMapper.valueToTree(schemaObj);
        String generated = generatorService.generateBody(schema);

        log.info("Request body generated successfully");

        return Map.of(
                "success", true,
                "body", generated
        );
    }
}

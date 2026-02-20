package io.github.rexrk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.rexrk.request.AiRequestBodyGeneratorService;
import io.github.rexrk.request.AiSwaggerHelperController;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@AutoConfiguration
@ConditionalOnClass({
        org.springdoc.webmvc.ui.SwaggerIndexTransformer.class,
        jakarta.servlet.http.HttpServletRequest.class,
})
@ConditionalOnProperty(
        prefix = AiSwaggerHelperProperties.prefix,
        name = "enabled",
        havingValue = BooleanUtils.TRUE,
        matchIfMissing = true
)
@EnableConfigurationProperties(AiSwaggerHelperProperties.class)
public class AiSwaggerHelperAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AiSwaggerHelperAutoConfiguration.class);
    private final AiSwaggerHelperProperties properties;

    public AiSwaggerHelperAutoConfiguration(AiSwaggerHelperProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    /**
     * 1️⃣ Spring-Ai configuration
     */
    @Bean
    @ConditionalOnMissingBean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }

    /**
     * 2️⃣ Service
     */

    @Bean
    public AiRequestBodyGeneratorService aiRequestBodyGeneratorService(
            ObjectProvider<ChatClient> chatClientObjectProvider,
            ObjectMapper objectMapper
    ) {
        return new AiRequestBodyGeneratorService(
                chatClientObjectProvider.getIfAvailable(),
                objectMapper,
                properties.getMode()
        );

    }

    /**
     * 3️⃣ Controller
     */
    @Bean
    public AiSwaggerHelperController aiSwaggerHelperController(
            AiRequestBodyGeneratorService service,
            ObjectMapper objectMapper) {
        return new AiSwaggerHelperController(service, objectMapper);

    }

    @Bean
    public RouterFunction<ServerResponse> aiSwaggerRoutes(
            AiSwaggerHelperController controller) {

        return RouterFunctions.route()
                .POST("/devtools/ai/request-body/generate",
                        RequestPredicates.contentType(MediaType.APPLICATION_JSON),
                        request -> {
                            try {
                                // Fetch request body
                                Map<String, Object> body = request.body(new ParameterizedTypeReference<>() {});
                                // Generate response for request body
                                Map<String, Object> response = controller.generateBody(body);

                                return ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(response);

                            } catch (IllegalArgumentException e) {
                                return ServerResponse.badRequest()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(Map.of(
                                                "success", false,
                                                "error", e.getMessage()
                                        ));

                            } catch (Exception e) {
                                return ServerResponse.status(500)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(Map.of(
                                                "success", false,
                                                "error", e.getMessage()
                                        ));
                            }
                        })
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(SwaggerIndexTransformer.class)
    public SwaggerIndexTransformer customSwaggerIndexTransformer(
            SwaggerUiConfigProperties swaggerUiConfig,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider) {

        return new SwaggerIndexPageTransformer(swaggerUiConfig, swaggerUiOAuthProperties, swaggerWelcomeCommon, objectMapperProvider) {

            @Override
            public Resource transform(HttpServletRequest request, Resource resource,
                                      ResourceTransformerChain chain) throws IOException {
                Resource transformed = super.transform(request, resource, chain);

                // Inject into index.html
                if (transformed.getFilename() != null && transformed.getFilename().equals("index.html")) {
                    String html = new String(transformed.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

                    // Inject plugin script BEFORE swagger-ui-bundle.js
                    String pluginScript = "<script src=\"/ai-generate-body-plugin.js\"></script>\n";
                    html = html.replace("<script src=\"./swagger-ui-bundle.js\"",
                            pluginScript + "    <script src=\"./swagger-ui-bundle.js\"");

                    byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
                    return new TransformedResource(transformed, bytes);
                }

                // Modify swagger-initializer.js to register the plugin
                if (transformed.getFilename() != null && transformed.getFilename().equals("swagger-initializer.js")) {
                    String js = new String(transformed.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

                    if (js.contains("plugins:")) {
                        js = js.replaceFirst(
                                "(plugins:\\s*\\[)",
                                "$1\n window.AiGenerateBodyPlugin,"
                        );
                    } else {
                        // If no plugins array exists, add it before layout
                        js = js.replaceFirst(
                                "(layout:)",
                                "plugins: [window.AiGenerateBodyPlugin],\n    $1"
                        );
                    }

                    byte[] bytes = js.getBytes(StandardCharsets.UTF_8);
                    return new TransformedResource(transformed, bytes);
                }

                return transformed;
            }
        };
    }

    @PostConstruct
    void logProps() {
        if (properties.isEnabled()) log.info("AI Swagger Helper enabled. Mode: {}", properties.getMode());
    }
}
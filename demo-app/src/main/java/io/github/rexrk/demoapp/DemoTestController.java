package io.github.rexrk.demoapp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoTestController {

    @PostMapping("/test/generate")
    @Operation(
        summary = "Test AI Swagger body generation",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TestRequest.class)
            )
        )
    )
    public String test(@org.springframework.web.bind.annotation.RequestBody TestRequest request) {
        return "OK";
    }

    // Dummy request body schema
    public static class TestRequest {
        public String name;
        public int age;
        public String email;
        public Address address;
    }

    public static class Address {
        public String city;
        public String country;
        public String zip;
    }
}

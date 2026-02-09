package io.quickledger.controllers.products;

import io.quickledger.dto.product.ProductDefinitionTabularDto;
import io.quickledger.services.product.JsonSchemaGenerator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/json-schema")
public class JsonSchemaGeneratorController {
    private final JsonSchemaGenerator jsonSchemaGenerator;

    public JsonSchemaGeneratorController(JsonSchemaGenerator jsonSchemaGenerator) {
        this.jsonSchemaGenerator = jsonSchemaGenerator;
    }

    @PostMapping("/generate-schema")
    public String generateSchema(@RequestBody ProductDefinitionTabularDto inputDto) {
        return jsonSchemaGenerator.generateJsonSchema(inputDto).toString();

    }
}

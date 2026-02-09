package io.quickledger.services.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.SpecVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class JsonSchemaValidator {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaValidator.class);

    public static class ValidationResult {
        private boolean valid;
        private String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        // getters and setters
        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    public ValidationResult validateJsonSchema(String jsonSchema, String jsonPayload) {
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            JsonSchema schema = factory.getSchema(jsonSchema);
            Set<ValidationMessage> errors = schema.validate(new com.fasterxml.jackson.databind.ObjectMapper().readTree(jsonPayload));

            if (errors.isEmpty()) {
                return new ValidationResult(true, "JSON is valid");
            } else {
                return new ValidationResult(false, "Invalid JSON: " + errors.toString());
            }
        } catch (Exception e) {
            return new ValidationResult(false, "Exception: " + e.getMessage());
        }
    }

    public List<JsonNode> updateJsonObjects(List<JsonNode> jsonObjects, JsonNode newSchemaNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

        try {
            // Create JSON schema instance
            JsonSchema newSchema = schemaFactory.getSchema(newSchemaNode);

            for (JsonNode jsonObject : jsonObjects) {
                // Validate the JSON object against the new schema
                Set<ValidationMessage> errors = newSchema.validate(jsonObject);

                for (ValidationMessage error : errors) {
                    String message = error.toString();
                    String attribute = message.substring(message.indexOf("#") + 1, message.indexOf(":")); // extract attribute name from message

                    // Handle adding new attributes
                    if (error.getMessage().contains("is missing but it is required")) {
                        ((ObjectNode) jsonObject).put(attribute, "default value"); // replace "default value" with the actual default value
                    }

                    // Handle deleting attributes
                    if (error.getMessage().contains("is not allowed")) {
                        ((ObjectNode) jsonObject).remove(attribute);
                    }

                    // Handle modifying attributes
                    if (error.getMessage().contains("type changed from")) {
                        String[] messageParts = error.getMessage().split(" ");
                        String originalType = messageParts[messageParts.length - 3];
                        String newType = messageParts[messageParts.length - 1];

                        // Block changes from String to number
                        if (originalType.equals("string") && newType.equals("number")) {
                            continue;
                        }

                        // Block changes from optional to mandatory
                        if (originalType.equals("optional") && newType.equals("mandatory")) {
                            continue;
                        }

                        // Allow all other changes
                        // Modify the attribute based on the new type
                        // Example: Add type conversion handling here
                    }
                }
            }

            // Return the updated JSON objects list
            return jsonObjects;

        } catch (Exception e) {
            logger.error("Error updating JSON objects: " + e.getMessage());
            // Handle exceptions
            return null;
        }
    }
}

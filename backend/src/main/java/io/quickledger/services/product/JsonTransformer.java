package io.quickledger.services.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
public class JsonTransformer {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static JsonNode updateJsonSchema(JsonNode oldSchema, JsonNode newSchema, JsonNode jsonInstance) throws Exception {

        // Preprocess the schema to ensure required fields have default values
        ensureRequiredFieldsHaveDefaults(newSchema);

        ObjectNode updatedJsonInstance = (ObjectNode) jsonInstance.deepCopy();

        // Remove fields not in the new schema
        removeOldFields(oldSchema, newSchema, updatedJsonInstance);

        // Add fields from the new schema if they don't exist in the instance
        addNewFields(newSchema, jsonInstance, updatedJsonInstance);

        // Validate the updated JSON instance against the new schema
        validateJson(updatedJsonInstance, newSchema);

        return updatedJsonInstance;
    }

    private static void ensureRequiredFieldsHaveDefaults(JsonNode schema) throws Exception {
        if (schema.has("properties")) {
            Iterator<Map.Entry<String, JsonNode>> fields = schema.get("properties").fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode fieldSchema = field.getValue();

/*           TODO GH: This is not a valid check and thus removed, a required field may have a default value, but schema validation will anyway take that!
                if (isFieldRequired(schema, fieldName) && !fieldSchema.has("default") && !fieldSchema.has("properties")) {
                    throw new Exception("Required field '" + fieldName + "' does not have a default value in the schema.");
                }
*/

                // Recursively check nested properties
                if (fieldSchema.has("properties")) {
                    ensureRequiredFieldsHaveDefaults(fieldSchema);
                }
            }
        }
    }

    private static void removeOldFields(JsonNode oldSchema, JsonNode newSchema, ObjectNode jsonInstance) {
        Iterator<Map.Entry<String, JsonNode>> oldFields = oldSchema.get("properties").fields();
        while (oldFields.hasNext()) {
            Map.Entry<String, JsonNode> oldField = oldFields.next();
            String fieldName = oldField.getKey();

            if (!newSchema.get("properties").has(fieldName)) {
                jsonInstance.remove(fieldName);
            } else if (oldField.getValue().has("properties")) {
                removeOldFields(oldField.getValue(), newSchema.get("properties").get(fieldName), (ObjectNode) jsonInstance.get(fieldName));
            }
        }
    }

    private static void addNewFields(JsonNode newSchema, JsonNode jsonInstance, ObjectNode updatedJsonInstance) throws Exception {
        Iterator<Map.Entry<String, JsonNode>> newFields = newSchema.get("properties").fields();
        while (newFields.hasNext()) {
            Map.Entry<String, JsonNode> newField = newFields.next();
            String fieldName = newField.getKey();
            JsonNode fieldSchema = newField.getValue();

            if (!updatedJsonInstance.has(fieldName)) {
                if (fieldSchema.has("default")) {
                    updatedJsonInstance.set(fieldName, fieldSchema.get("default"));
                } else if (fieldSchema.has("properties")) {
                    ObjectNode nestedObject = mapper.createObjectNode();
                    updatedJsonInstance.set(fieldName, nestedObject);
                    addNewFields(fieldSchema, mapper.createObjectNode(), nestedObject);
                }
            } else if (fieldSchema.has("properties")) {
                addNewFields(fieldSchema, jsonInstance.get(fieldName), (ObjectNode) updatedJsonInstance.get(fieldName));

            }
        }
    }

    private static void validateJson(JsonNode jsonInstance, JsonNode schemaNode) throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(schemaNode.toString());

        Set<ValidationMessage> validationMessages = schema.validate(jsonInstance);
        if (!validationMessages.isEmpty()) {
            throw new Exception("JSON validation against new schema failed: " + validationMessages);
        }
    }

}

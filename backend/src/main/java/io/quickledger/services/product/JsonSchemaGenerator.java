package io.quickledger.services.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quickledger.dto.product.ProductDefinitionDto;
import io.quickledger.dto.product.ProductDefinitionTabularDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

@Component
public class JsonSchemaGenerator {
    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaGenerator.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode generateJsonSchema(ProductDefinitionTabularDto inputDto) {
        JsonNode jsonSchema = null;
        jsonSchema = generateSchemaSimplified(inputDto);
        logger.debug("Generated JSON Schema: {}", jsonSchema.toPrettyString());
        return jsonSchema;
    }

    private JsonNode generateSchemaSimplified(ProductDefinitionTabularDto inputDto) {
        logger.debug("InputDto: {}", inputDto.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode outputNode = objectMapper.createObjectNode();

        //JsonNode titleNode = inputNode.get("name");
        if (StringUtils.hasText(inputDto.getName())) {
            outputNode.put("title", inputDto.getName());
        }

        //JsonNode descriptionNode = inputNode.get("productDescription");
        if (StringUtils.hasText(inputDto.getProductDescription())) {
            outputNode.put("description", inputDto.getProductDescription());
        }

        ObjectNode propertiesNode = outputNode.putObject("properties");

        //ArrayNode propertiesArray = (ArrayNode) inputNode.get("rows");
        //for (JsonNode property : propertiesArray) {
        for(ProductDefinitionTabularDto.TableRow row : inputDto.getRows()) {
            //JsonNode nameNode = property.get("name");

            ObjectNode propertyNode = propertiesNode.putObject(row.getName());
            propertyNode.put("type", "object");

            //JsonNode requiredNode = property.get("required");
            if (StringUtils.hasText(row.getRequired())) {
                propertyNode.put("required", "yes".equals(row.getRequired()));
            } else {
                propertyNode.put("required", false);
            }

            //JsonNode propertyDescriptionNode = property.get("description");
            if (StringUtils.hasText(row.getDescription())) {
                propertyNode.put("description", row.getDescription());
            }

            if (StringUtils.hasText(row.getPattern())) {
                propertyNode.put("pattern", row.getPattern());
            }

            //if (!property.isEmpty()) { why this check? RG
                ObjectNode propertyPropertiesNode = propertyNode.putObject("properties");
                ObjectNode valueNode = propertyPropertiesNode.putObject("value"); //hardcoded value node which holds the type and value of property
                valueNode.put("type", row.getValueType());
                valueNode.put("required", "yes".equals(row.getRequired())); //required is same as the parent required as this is value of the property

                //JsonNode valueDescriptionNode = property.get("description");
                if (StringUtils.hasText(row.getDescription())) {
                    valueNode.put("description", row.getDescription());
                }

                if (StringUtils.hasText(row.getDefaultValue())) {
                    if(row.getValueType().equals("string")) {
                        valueNode.put("default", row.getDefaultValue());
                    } else if(row.getValueType().equals("number")) {
                        valueNode.put("default", Integer.parseInt(row.getDefaultValue()));
                    }
                }

                ObjectNode unitNode = null;
                if (StringUtils.hasText(row.getUnit())) {
                    unitNode = propertyPropertiesNode.putObject("unit");
                    unitNode.put("type", "string");
                    unitNode.put("required", true);

                    if (StringUtils.hasText(row.getDefaultValue())) {
                        valueNode.put("defaultValue", row.getDefaultValue());
                    }

                    ArrayNode enumNode = unitNode.putArray("enum");
                    String[] units = row.getUnit().split(",");
                    for (String unit : units) {
                        enumNode.add(unit.trim());
                    }
                }

//            } else {
//                // If there is no "unit" field, we still need to create an empty "properties" object
//                propertyNode.putObject("properties");
//            }
        }
        logger.debug("OutputNode: {}", outputNode.toPrettyString());
        return generateSchema(outputNode);
    }

    /* INFO: This one works with example payload following standard schema similar to json-schema.org
    {
        "title": "ManiWire",
        "description": "this is some test description that can be used in future like add assetId here or something usefull for our internal",
        "properties": {
            "wireLength": {
                "type": "object",
                "required": true,
                "description": "Length of the wire including the unit of measurement",
                "pattern": "^[a-zA-Z]+$",
                "properties": {
                    "value": {
                        "type": "number",
                        "required": true,
                        "description": "Numerical value of the length"
                    },
                    "unit": {
                        "type": "string",
                        "required": true,
                        "pattern": "^(cm|mm|m)$",
                        "description": "Unit of measurement for the wire length",
                        "enum": [
                            "cm",
                            "mm",
                            "m"
                        ]
                    }
                }
            },
            "operatingTemperature": {
                "type": "object",
                "required": true,
                "description": "Operating temperature range of the device",
                "properties": {
                    "minTemperature": {
                        "type": "number",
                        "required": true,
                        "description": "Minimum operating temperature",
                        "defaultValue": 0
                    },
                    "maxTemperature": {
                        "type": "number",
                        "required": true,
                        "description": "Maximum operating temperature",
                        "defaultValue": 100
                    },
                    "unit": {
                        "type": "string",
                        "required": true,
                        "description": "Unit of measurement for temperature",
                        "enum": [
                            "C",
                            "F"
                        ]
                    }
                }
            }
        }
    }

     */
    private JsonNode generateSchema(JsonNode rootNode) {
        ObjectNode schemaNode = objectMapper.createObjectNode();
        //react-jsonschema-form does not like $schema to be present in the schema so i removed it. RG ->> MBH we need to check this as it's important each schema version can be different we need to make sure we are following 2020 version!
        // TODO Let's not remove it here we better remove it in other method or service before sending it back to GUI we better have clean implementation for APIs and in Database. I think we need to maintain these codes and one day we may get GUI developer and we can ask them to fix them all :D
        schemaNode.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        schemaNode.put("type", "object");
        schemaNode.put("title", rootNode.get("title").asText());
        schemaNode.put("description", rootNode.get("description").asText());
        ObjectNode propertiesNode = schemaNode.putObject("properties");
        ArrayNode rootRequiredArray = objectMapper.createArrayNode();

        JsonNode propertiesNodeInput = rootNode.get("properties");
        Iterator<String> attributeNames = propertiesNodeInput.fieldNames();

        while (attributeNames.hasNext()) {
            String attributeName = attributeNames.next();
            JsonNode attributeNode = propertiesNodeInput.get(attributeName);
            ObjectNode attributePropertiesNode = propertiesNode.putObject(attributeName);
            attributePropertiesNode.put("type", attributeNode.get("type").asText());
            attributePropertiesNode.put("description", attributeNode.get("description").asText());

            if (attributeNode.has("pattern")) {
                attributePropertiesNode.put("pattern", attributeNode.get("pattern").asText());
            }

            ArrayNode requiredArray = objectMapper.createArrayNode();
            if (attributeNode.has("properties")) {
                JsonNode properties = attributeNode.get("properties");
                ObjectNode propertiesPropertiesNode = attributePropertiesNode.putObject("properties");
                Iterator<String> fieldNames = properties.fieldNames();

                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode fieldNode = properties.get(fieldName);
                    ObjectNode fieldPropertiesNode = propertiesPropertiesNode.putObject(fieldName);
                    fieldPropertiesNode.put("type", fieldNode.get("type").asText());
                    if (fieldNode.has("description")) {
                        fieldPropertiesNode.put("description", fieldNode.get("description").asText());
                    }

                    if (fieldNode.has("enum")) {
                        JsonNode enumNodeJson = fieldNode.get("enum");
                        ArrayNode enumNode = fieldPropertiesNode.putArray("enum");
                        if (enumNodeJson.isArray()) {
                            enumNodeJson.forEach(enumValue -> {
                                enumNode.add(enumValue.asText());
                            });
                        } else {
                            enumNode.add(enumNodeJson.asText());
                        }
                    }

                    if (fieldNode.has("default")) {
                        fieldPropertiesNode.set("default", fieldNode.get("default"));
                    }

                    if (fieldNode.has("required") && fieldNode.get("required").asBoolean()) {
                        requiredArray.add(fieldName);
                    }

                    if (fieldNode.has("pattern")) {
                        fieldPropertiesNode.put("pattern", fieldNode.get("pattern").asText());
                    }
                }
            }

            if (requiredArray.size() > 0) {
                attributePropertiesNode.set("required", requiredArray);
            }

            if (attributeNode.has("required") && attributeNode.get("required").asBoolean()) {
                rootRequiredArray.add(attributeName);
            }
        }

        if (rootRequiredArray.size() > 0) {
            schemaNode.set("required", rootRequiredArray);
        }

        return schemaNode;
    }

    /* INFO: This one works with following schema:
    {
        "title": "Asset",
        "description": "Asset description",
        "properties": [
            {
                "attributeName": "attribute1",
                "type": "string",
                "description": "Attribute 1 description",
                "required": true
            },
            {
                "attributeName": "attribute2",
                "type": "object",
                "description": "Attribute 2 description",
                "properties": [
                    {
                        "fieldName": "field1",
                        "type": "string",
                        "description": "Field 1 description",
                        "required": true
                    },
                    {
                        "fieldName": "field2",
                        "type": "string",
                        "description": "Field 2 description",
                        "enum": ["value1", "value2"]
                    }
                ]
            }
        ]
     */

    private JsonNode generateSchemaschemaOld(JsonNode rootNode) {
        ObjectNode schemaNode = objectMapper.createObjectNode();
        schemaNode.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        schemaNode.put("type", "object");
        schemaNode.put("title", rootNode.get("title").asText());
        schemaNode.put("description", rootNode.get("description").asText());
        schemaNode.put("type", "object");
        ObjectNode propertiesNode = schemaNode.putObject("properties");
        ArrayNode rootRequiredArray = objectMapper.createArrayNode();

        JsonNode attributesNode = rootNode.get("properties");

        if (attributesNode.isArray()) {
            for (JsonNode attributeNode : attributesNode) {
                String attributeName = attributeNode.get("attributeName").asText();
                ObjectNode attributePropertiesNode = propertiesNode.putObject(attributeName);
                attributePropertiesNode.put("type", attributeNode.get("type").asText());
                attributePropertiesNode.put("description", attributeNode.get("description").asText());

                if (attributeNode.has("pattern")) {
                    attributePropertiesNode.put("pattern", attributeNode.get("pattern").asText());
                }

                ArrayNode requiredArray = objectMapper.createArrayNode();
                if (attributeNode.has("properties")) {
                    JsonNode properties = attributeNode.get("properties");
                    ObjectNode propertiesPropertiesNode = attributePropertiesNode.putObject("properties");
                    Iterator<String> fieldNames = properties.fieldNames();

                    while (fieldNames.hasNext()) {
                        String fieldName = fieldNames.next();
                        JsonNode fieldNode = properties.get(fieldName);
                        ObjectNode fieldPropertiesNode = propertiesPropertiesNode.putObject(fieldName);
                        fieldPropertiesNode.put("type", fieldNode.get("type").asText());
                        fieldPropertiesNode.put("description", fieldNode.get("description").asText());

                        if (fieldNode.has("enum")) {
                            ArrayNode enumNode = fieldPropertiesNode.putArray("enum");
                            fieldNode.get("enum").forEach(enumValue -> {
                                enumNode.add(enumValue.asText());
                            });
                        }

                        if (fieldNode.has("defaultValue")) {
                            fieldPropertiesNode.set("default", fieldNode.get("defaultValue"));
                        }

                        if (fieldNode.has("required") && fieldNode.get("required").asBoolean()) {
                            requiredArray.add(fieldName);
                        }

                        if (fieldNode.has("pattern")) {
                            fieldPropertiesNode.put("pattern", fieldNode.get("pattern").asText());
                        }
                    }
                }

                if (requiredArray.size() > 0) {
                    attributePropertiesNode.set("required", requiredArray);
                }

                if (attributeNode.has("required") && attributeNode.get("required").asBoolean()) {
                    rootRequiredArray.add(attributeName);
                }
            }
        }

        if (rootRequiredArray.size() > 0) {
            schemaNode.set("required", rootRequiredArray);
        }

        return schemaNode;
    }


    /**
        convert a json schema dto to a tabular format dto for UI display
     */
    public ProductDefinitionTabularDto convertJsonSchemaToTabular(ProductDefinitionDto productDefinitionDto) {

        logger.debug(productDefinitionDto.toString());

        ProductDefinitionTabularDto output = new ProductDefinitionTabularDto();
        if(productDefinitionDto.getProductAttributes().isEmpty()) {
            return output;
        }

        if(productDefinitionDto.getProductAttributes().get(0).get("schema") == null) {
            return output;
        }

        HashMap schema = (HashMap)productDefinitionDto.getProductAttributes().get(0).get("schema");


        output.setName(productDefinitionDto.getName());
        output.setProductDescription(productDefinitionDto.getProductDescription());
        output.setId(productDefinitionDto.getId());

        List <ProductDefinitionTabularDto.TableRow> rows = new ArrayList<>();
        output.setRows(rows);

        if (schema.containsKey("properties")) {
            LinkedHashMap<String, Object> properties = (LinkedHashMap<String, Object>) schema.get("properties");
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String fieldName = entry.getKey();
                LinkedHashMap<String, Object> fieldNode = (LinkedHashMap<String, Object>) entry.getValue();
                ProductDefinitionTabularDto.TableRow row = new ProductDefinitionTabularDto.TableRow();
                rows.add(row);

                row.setName(fieldName);
                row.setDescription(fieldNode.get("description").toString());

                if (fieldNode.containsKey("required")) {
                    row.setRequired("yes");
                    HashMap nestedproperties = (HashMap) fieldNode.get("properties");
                    HashMap unitmap = (HashMap) nestedproperties.get("unit");
                    if(unitmap !=null){
                        List enumlist = (List)unitmap.get("enum");
                        String enumvalues = String.join(",", enumlist);
                        row.setUnit(enumvalues);
                    }
                    HashMap valuemap = (HashMap) nestedproperties.get("value");
                    row.setValueType(valuemap.get("type").toString());
                }else {
                    row.setRequired("no");
                    HashMap nestedproperties = (HashMap) fieldNode.get("properties");
                    HashMap valuemap = (HashMap) nestedproperties.get("value");
                    row.setValueType(valuemap.get("type").toString());
                    if (valuemap.containsKey("default"))  row.setDefaultValue(valuemap.get("default").toString());
                }
            }
        }

        logger.debug(output.toString());
        return output;
    }
}

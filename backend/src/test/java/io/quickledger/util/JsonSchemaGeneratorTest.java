package io.quickledger.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quickledger.dto.product.ProductDefinitionDto;
import io.quickledger.dto.product.ProductDefinitionTabularDto;
import io.quickledger.services.product.JsonSchemaGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonSchemaGeneratorTest {

    JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator();

    @Test
    void convertJsonSchemaToTabularTest(){

        String jsonString2 = """
                {
                        "schema": {
                          "type": "object",
                          "title": "Mani-test2",
                          "$schema": "https://json-schema.org/draft/2020-12/schema",
                          "required": [
                            "Length",
                            "roll weight"
                          ],
                          "properties": {
                            "Length": {
                              "type": "object",
                              "required": [
                                "value",
                                "unit"
                              ],
                              "properties": {
                                "unit": {
                                  "enum": [
                                    "m",
                                    "cm",
                                    "mm"
                                  ],
                                  "type": "string"
                                },
                                "value": {
                                  "type": "number",
                                  "description": "length"
                                }
                              },
                              "description": "length"
                            },
                            "roll weight": {
                              "type": "object",
                              "required": [
                                "value",
                                "unit"
                              ],
                              "properties": {
                                "unit": {
                                  "enum": [
                                    "gr",
                                    "kg"
                                  ],
                                  "type": "string"
                                },
                                "value": {
                                  "type": "number",
                                  "description": "roll weight"
                                }
                              },
                              "description": "roll weight"
                            },
                            "specification url": {
                              "type": "object",
                              "properties": {
                                "value": {
                                  "type": "string",
                                  "description": "spec url"
                                }
                              },
                              "description": "spec url"
                            }
                          },
                          "description": "awesome write"
                        }
                      }
                """;
        String jsonString = """
                
                {
                    "schema": {
                      "type": "object",
                      "title": "wire",
                      "$schema": "https://json-schema.org/draft/2020-12/schema",
                      "required": [
                        "Length"
                      ],
                      "properties": {
                        "Length": {
                          "type": "object",
                          "required": [
                            "value",
                            "unit"
                          ],
                          "properties": {
                            "unit": {
                              "enum": [
                                "m"
                              ],
                              "type": "string"
                            },
                            "value": {
                              "type": "string",
                              "description": ""
                            }
                          },
                          "description": ""
                        }
                      },
                      "description": "abc"
                    }
                  }
                
                """;
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, Object> map = new HashMap<>();
        try {
            map = objectMapper.readValue(jsonString2, LinkedHashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //convert json to hashmap
        ProductDefinitionDto productDefinitionDto = new ProductDefinitionDto();
        productDefinitionDto.setName("wire");
        productDefinitionDto.setProductDescription("abc");
        productDefinitionDto.setProductAttributes(List.of(Map.of("schema", map.get("schema"))));
        ProductDefinitionTabularDto out = jsonSchemaGenerator.convertJsonSchemaToTabular(productDefinitionDto);
        System.out.println(out);
    }
}

package io.quickledger.services.product;

import com.google.gson.Gson;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Map;

/*
 * INFO:
 * This class is used to convert a Map<String, Object> to a JSON formatted string
 * and vice versa. This is useful when you want to store a Json in a database column
 */
@Converter(autoApply = true)
public class JsonConverterGson implements AttributeConverter<Map<String, Object>, String> {

    private final static Gson gson = new Gson();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> meta) {
        return gson.toJson(meta);
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        return gson.fromJson(dbData, Map.class);
    }
}
package io.quickledger.dto.voice;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VoiceDocumentType {
    INVOICE,
    QUOTE,
    UNKNOWN;

    @JsonCreator
    public static VoiceDocumentType fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        String normalized = value.trim().toUpperCase();
        if ("INVOICE".equals(normalized)) {
            return INVOICE;
        }
        if ("QUOTE".equals(normalized)) {
            return QUOTE;
        }
        return UNKNOWN;
    }
}

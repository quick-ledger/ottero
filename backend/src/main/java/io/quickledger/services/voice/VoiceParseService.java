package io.quickledger.services.voice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quickledger.dto.voice.VoiceDocumentType;
import io.quickledger.dto.voice.VoiceLineItem;
import io.quickledger.dto.voice.VoiceParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VoiceParseService {
    private static final Logger logger = LoggerFactory.getLogger(VoiceParseService.class);
    private static final Pattern LINE_ITEM_PATTERN = Pattern.compile("(?i)([a-zA-Z0-9][a-zA-Z0-9\\s\\-']+?)\\s*(?:for|at|=|:)?\\s*\\$\\s*(\\d+(?:\\.\\d{1,2})?)");
    private static final Pattern CLIENT_PATTERN = Pattern.compile("(?i)\\bto\\s+([a-zA-Z][a-zA-Z\\s'\\-]+)");

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    public VoiceParseService(OllamaClient ollamaClient, ObjectMapper objectMapper) {
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
    }

    public VoiceParseResult parseTranscript(String transcript) {
        VoiceParseResult result = null;
        String prompt = buildPrompt(transcript);

        try {
            String json = ollamaClient.generateJson(prompt, buildSystemPrompt());
            result = objectMapper.readValue(json, VoiceParseResult.class);
        } catch (Exception e) {
            logger.warn("LLM parse failed, falling back to rule-based parsing", e);
            result = new VoiceParseResult();
        }

        applyRuleBasedFallback(transcript, result);
        return result;
    }

    private void applyRuleBasedFallback(String transcript, VoiceParseResult result) {
        if (result.getDocumentType() == null || result.getDocumentType() == VoiceDocumentType.UNKNOWN) {
            String normalized = transcript == null ? "" : transcript.toLowerCase();
            if (normalized.contains("invoice")) {
                result.setDocumentType(VoiceDocumentType.INVOICE);
            } else if (normalized.contains("quote")) {
                result.setDocumentType(VoiceDocumentType.QUOTE);
            }
        }

        if (result.getClientName() == null || result.getClientName().isBlank()) {
            Matcher matcher = CLIENT_PATTERN.matcher(transcript == null ? "" : transcript);
            if (matcher.find()) {
                result.setClientName(matcher.group(1).trim());
            }
        }

        if (result.getLineItems() == null || result.getLineItems().isEmpty()) {
            List<VoiceLineItem> lineItems = new ArrayList<>();
            Matcher matcher = LINE_ITEM_PATTERN.matcher(transcript == null ? "" : transcript);
            while (matcher.find()) {
                String description = matcher.group(1).trim();
                String amountStr = matcher.group(2).trim();
                VoiceLineItem item = new VoiceLineItem();
                item.setDescription(description);
                item.setAmount(new BigDecimal(amountStr));
                item.setQuantity(1);
                lineItems.add(item);
            }
            if (!lineItems.isEmpty()) {
                result.setLineItems(lineItems);
            }
        }
    }

    private String buildSystemPrompt() {
        return "You extract structured invoice/quote data from transcripts. " +
                "Return only valid JSON that matches this schema: " +
                "{ \"documentType\": \"invoice|quote|unknown\", " +
                "\"clientName\": \"string or null\", " +
                "\"lineItems\": [{\"description\": \"string\", \"amount\": number, \"quantity\": number}], " +
                "\"notes\": \"string or null\", " +
                "\"requestedAction\": \"draft|send|unknown\" }";
    }

    private String buildPrompt(String transcript) {
        return "Transcript: " + transcript;
    }
}

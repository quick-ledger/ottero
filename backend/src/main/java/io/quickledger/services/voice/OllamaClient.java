package io.quickledger.services.voice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quickledger.config.VoiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class OllamaClient {
    private static final Logger logger = LoggerFactory.getLogger(OllamaClient.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final VoiceProperties voiceProperties;

    public OllamaClient(ObjectMapper objectMapper, VoiceProperties voiceProperties) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.voiceProperties = voiceProperties;
    }

    public String generateJson(String prompt, String system) {
        String baseUrl = normalizeBaseUrl(voiceProperties.getLlm().getBaseUrl());
        String model = voiceProperties.getLlm().getModel();
        int timeoutMs = voiceProperties.getLlm().getTimeoutMs();

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("prompt", prompt);
        payload.put("system", system);
        payload.put("stream", false);
        payload.put("format", "json");
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0);
        payload.put("options", options);

        try {
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                logger.error("Ollama request failed: status={} body={}", response.statusCode(), response.body());
                throw new IllegalStateException("LLM request failed with status " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode responseNode = root.get("response");
            if (responseNode == null || responseNode.isNull()) {
                logger.error("Ollama response missing 'response' field: {}", response.body());
                throw new IllegalStateException("LLM response missing 'response' field");
            }
            return responseNode.asText();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("LLM request interrupted", e);
            throw new IllegalStateException("LLM request interrupted", e);
        } catch (IOException e) {
            logger.error("LLM request failed", e);
            throw new IllegalStateException("LLM request failed", e);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:11434";
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}

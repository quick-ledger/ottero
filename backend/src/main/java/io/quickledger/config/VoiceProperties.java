package io.quickledger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "voice")
public class VoiceProperties {
    private final Stt stt = new Stt();
    private final Llm llm = new Llm();

    public Stt getStt() {
        return stt;
    }

    public Llm getLlm() {
        return llm;
    }

    public static class Stt {
        private String languageCode = "en-US";
        private Integer sampleRateHertz = 16000;
        private String encoding = "LINEAR16";

        public String getLanguageCode() {
            return languageCode;
        }

        public void setLanguageCode(String languageCode) {
            this.languageCode = languageCode;
        }

        public Integer getSampleRateHertz() {
            return sampleRateHertz;
        }

        public void setSampleRateHertz(Integer sampleRateHertz) {
            this.sampleRateHertz = sampleRateHertz;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }
    }

    public static class Llm {
        private String baseUrl = "http://localhost:11434";
        private String model = "qwen2.5:3b";
        private Integer timeoutMs = 20000;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public Integer getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(Integer timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }
}

package io.quickledger.services.voice;

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import io.quickledger.config.VoiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GoogleSpeechToTextService implements SpeechToTextService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleSpeechToTextService.class);
    private final VoiceProperties voiceProperties;

    public GoogleSpeechToTextService(VoiceProperties voiceProperties) {
        this.voiceProperties = voiceProperties;
    }

    @Override
    public String transcribe(byte[] audioBytes, String languageCode, Integer sampleRateHertz, String encoding) {
        String resolvedLanguage = languageCode != null ? languageCode : voiceProperties.getStt().getLanguageCode();
        Integer resolvedSampleRate = sampleRateHertz != null ? sampleRateHertz : voiceProperties.getStt().getSampleRateHertz();
        String resolvedEncoding = encoding != null ? encoding : voiceProperties.getStt().getEncoding();

        RecognitionConfig.AudioEncoding audioEncoding = parseEncoding(resolvedEncoding);

        RecognitionConfig.Builder configBuilder = RecognitionConfig.newBuilder()
                .setLanguageCode(resolvedLanguage)
                .setEnableAutomaticPunctuation(true);

        if (audioEncoding != null) {
            configBuilder.setEncoding(audioEncoding);
        }
        if (resolvedSampleRate != null) {
            configBuilder.setSampleRateHertz(resolvedSampleRate);
        }

        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(ByteString.copyFrom(audioBytes))
                .build();

        try (SpeechClient speechClient = SpeechClient.create()) {
            RecognizeResponse response = speechClient.recognize(configBuilder.build(), audio);
            StringBuilder transcript = new StringBuilder();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                if (result.getAlternativesCount() == 0) {
                    continue;
                }
                SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                if (!alternative.getTranscript().isBlank()) {
                    if (transcript.length() > 0) {
                        transcript.append(" ");
                    }
                    transcript.append(alternative.getTranscript().trim());
                }
            }
            return transcript.toString();
        } catch (IOException e) {
            logger.error("Failed to initialize Google Speech client", e);
            throw new IllegalStateException("Unable to initialize speech-to-text client", e);
        }
    }

    private RecognitionConfig.AudioEncoding parseEncoding(String encoding) {
        if (encoding == null || encoding.isBlank()) {
            return null;
        }
        try {
            return RecognitionConfig.AudioEncoding.valueOf(encoding.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            logger.warn("Unknown audio encoding '{}', leaving unset", encoding);
            return null;
        }
    }
}

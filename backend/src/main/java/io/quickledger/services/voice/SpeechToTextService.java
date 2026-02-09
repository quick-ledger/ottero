package io.quickledger.services.voice;

public interface SpeechToTextService {
    String transcribe(byte[] audioBytes, String languageCode, Integer sampleRateHertz, String encoding);
}

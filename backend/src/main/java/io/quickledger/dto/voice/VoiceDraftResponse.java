package io.quickledger.dto.voice;

import io.quickledger.dto.invoice.InvoiceDto;
import io.quickledger.dto.quote.QuoteDto;

import java.util.ArrayList;
import java.util.List;

public class VoiceDraftResponse {
    private String transcript;
    private VoiceParseResult parseResult;
    private InvoiceDto invoice;
    private QuoteDto quote;
    private List<String> warnings = new ArrayList<>();

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public VoiceParseResult getParseResult() {
        return parseResult;
    }

    public void setParseResult(VoiceParseResult parseResult) {
        this.parseResult = parseResult;
    }

    public InvoiceDto getInvoice() {
        return invoice;
    }

    public void setInvoice(InvoiceDto invoice) {
        this.invoice = invoice;
    }

    public QuoteDto getQuote() {
        return quote;
    }

    public void setQuote(QuoteDto quote) {
        this.quote = quote;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}

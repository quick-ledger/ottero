package io.quickledger.dto.voice;

import java.util.ArrayList;
import java.util.List;

public class VoiceParseResult {
    private VoiceDocumentType documentType = VoiceDocumentType.UNKNOWN;
    private String clientName;
    private List<VoiceLineItem> lineItems = new ArrayList<>();
    private String notes;
    private String requestedAction;

    public VoiceDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(VoiceDocumentType documentType) {
        this.documentType = documentType;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public List<VoiceLineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<VoiceLineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRequestedAction() {
        return requestedAction;
    }

    public void setRequestedAction(String requestedAction) {
        this.requestedAction = requestedAction;
    }
}

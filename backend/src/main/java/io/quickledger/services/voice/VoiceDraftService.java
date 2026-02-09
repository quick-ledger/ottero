package io.quickledger.services.voice;

import io.quickledger.dto.invoice.InvoiceDto;
import io.quickledger.dto.invoice.InvoiceItemDto;
import io.quickledger.dto.quote.QuoteDto;
import io.quickledger.dto.quote.QuoteItemDto;
import io.quickledger.dto.voice.VoiceDocumentType;
import io.quickledger.dto.voice.VoiceDraftResponse;
import io.quickledger.dto.voice.VoiceLineItem;
import io.quickledger.dto.voice.VoiceParseResult;
import io.quickledger.entities.User;
import io.quickledger.entities.invoice.Invoice;
import io.quickledger.entities.quote.Quote;
import io.quickledger.services.InvoiceService;
import io.quickledger.services.QuoteService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class VoiceDraftService {
    private final SpeechToTextService speechToTextService;
    private final VoiceParseService voiceParseService;
    private final InvoiceService invoiceService;
    private final QuoteService quoteService;

    public VoiceDraftService(SpeechToTextService speechToTextService,
            VoiceParseService voiceParseService,
            InvoiceService invoiceService,
            QuoteService quoteService) {
        this.speechToTextService = speechToTextService;
        this.voiceParseService = voiceParseService;
        this.invoiceService = invoiceService;
        this.quoteService = quoteService;
    }

    public VoiceDraftResponse createDraftFromAudio(Long companyId,
            MultipartFile audio,
            String documentTypeOverride,
            String languageCode,
            Integer sampleRateHertz,
            String encoding,
            User user) {
        try {
            String transcript = speechToTextService.transcribe(
                    audio.getBytes(),
                    languageCode,
                    sampleRateHertz,
                    encoding);

            VoiceParseResult parseResult = voiceParseService.parseTranscript(transcript);
            VoiceDocumentType overrideType = VoiceDocumentType.fromString(documentTypeOverride);
            if (overrideType != VoiceDocumentType.UNKNOWN) {
                parseResult.setDocumentType(overrideType);
            }

            VoiceDraftResponse response = new VoiceDraftResponse();
            response.setTranscript(transcript);
            response.setParseResult(parseResult);
            response.setWarnings(buildWarnings(parseResult));

            if (parseResult.getDocumentType() == VoiceDocumentType.QUOTE) {
                QuoteDto draft = buildQuoteDto(parseResult, companyId, user);
                QuoteDto saved = quoteService.createOrUpdateQuote(draft, companyId, user.getId());
                response.setQuote(saved);
            } else {
                InvoiceDto draft = buildInvoiceDto(parseResult, companyId, user);
                InvoiceDto saved = invoiceService.createUpdate(companyId, draft, user);
                response.setInvoice(saved);
            }

            return response;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read audio input", e);
        }
    }

    private List<String> buildWarnings(VoiceParseResult parseResult) {
        List<String> warnings = new ArrayList<>();
        if (parseResult.getClientName() == null || parseResult.getClientName().isBlank()) {
            warnings.add("No client name detected.");
        }
        if (parseResult.getLineItems() == null || parseResult.getLineItems().isEmpty()) {
            warnings.add("No line items detected.");
        }
        if ("send".equalsIgnoreCase(parseResult.getRequestedAction())) {
            warnings.add("Voice requested sending; draft was created instead.");
        }
        return warnings;
    }

    private InvoiceDto buildInvoiceDto(VoiceParseResult parseResult, Long companyId, User user) {
        InvoiceDto dto = new InvoiceDto();
        dto.setCompanyId(companyId);
        dto.setUserId(user.getId());
        dto.setStatus(Invoice.InvoiceStatus.DRAFT);
        applyClientName(parseResult.getClientName(), dto, null);
        dto.setInvoiceItems(buildInvoiceItems(parseResult.getLineItems()));
        dto.setTotalPrice(calculateTotal(dto.getInvoiceItems()));
        dto.setGst(BigDecimal.ZERO);
        dto.setNotes(parseResult.getNotes());
        return dto;
    }

    private QuoteDto buildQuoteDto(VoiceParseResult parseResult, Long companyId, User user) {
        QuoteDto dto = new QuoteDto();
        dto.setCompanyId(companyId);
        dto.setUserId(user.getId());
        dto.setStatus(Quote.QuoteStatus.PENDING);
        applyClientName(parseResult.getClientName(), null, dto);
        dto.setQuoteItems(buildQuoteItems(parseResult.getLineItems()));
        dto.setTotalPrice(calculateTotal(dto.getQuoteItems()));
        dto.setGst(BigDecimal.ZERO);
        dto.setNotes(parseResult.getNotes());
        return dto;
    }

    private List<InvoiceItemDto> buildInvoiceItems(List<VoiceLineItem> lineItems) {
        List<InvoiceItemDto> items = new ArrayList<>();
        if (lineItems == null) {
            return items;
        }
        int order = 0;
        for (VoiceLineItem item : lineItems) {
            if (item.getAmount() == null || item.getDescription() == null) {
                continue;
            }
            int quantity = item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1;
            BigDecimal total = item.getAmount().multiply(BigDecimal.valueOf(quantity));
            InvoiceItemDto dto = new InvoiceItemDto();
            dto.setItemDescription(item.getDescription());
            dto.setPrice(item.getAmount());
            dto.setQuantity(quantity);
            dto.setTotal(total);
            dto.setGst(BigDecimal.ZERO);
            dto.setItemOrder(order++);
            items.add(dto);
        }
        return items;
    }

    private List<QuoteItemDto> buildQuoteItems(List<VoiceLineItem> lineItems) {
        List<QuoteItemDto> items = new ArrayList<>();
        if (lineItems == null) {
            return items;
        }
        int order = 0;
        for (VoiceLineItem item : lineItems) {
            if (item.getAmount() == null || item.getDescription() == null) {
                continue;
            }
            int quantity = item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1;
            BigDecimal total = item.getAmount().multiply(BigDecimal.valueOf(quantity));
            QuoteItemDto dto = new QuoteItemDto();
            dto.setItemDescription(item.getDescription());
            dto.setPrice(item.getAmount());
            dto.setQuantity(quantity);
            dto.setTotal(total);
            dto.setGst(BigDecimal.ZERO);
            dto.setItemOrder(order++);
            items.add(dto);
        }
        return items;
    }

    private BigDecimal calculateTotal(List<?> items) {
        BigDecimal total = BigDecimal.ZERO;
        if (items == null) {
            return total;
        }
        for (Object obj : items) {
            if (obj instanceof InvoiceItemDto invoiceItem) {
                total = total.add(invoiceItem.getTotal() != null ? invoiceItem.getTotal() : BigDecimal.ZERO);
            } else if (obj instanceof QuoteItemDto quoteItem) {
                total = total.add(quoteItem.getTotal() != null ? quoteItem.getTotal() : BigDecimal.ZERO);
            }
        }
        return total;
    }

    private void applyClientName(String clientName, InvoiceDto invoiceDto, QuoteDto quoteDto) {
        if (clientName == null || clientName.isBlank()) {
            return;
        }
        String trimmed = clientName.trim();
        String[] parts = trimmed.split("\\s+", 2);
        if (invoiceDto != null) {
            if (parts.length == 2) {
                invoiceDto.setClientFirstname(parts[0]);
                invoiceDto.setClientLastname(parts[1]);
            } else {
                invoiceDto.setClientFirstname(trimmed);
            }
        }
        if (quoteDto != null) {
            if (parts.length == 2) {
                quoteDto.setClientFirstname(parts[0]);
                quoteDto.setClientLastname(parts[1]);
            } else {
                quoteDto.setClientFirstname(trimmed);
            }
        }
    }
}

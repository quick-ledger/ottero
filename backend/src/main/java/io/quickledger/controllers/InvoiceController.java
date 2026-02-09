package io.quickledger.controllers;

import io.quickledger.dto.invoice.InvoiceDto;
import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.InvoiceService;
import jakarta.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies/{companyId}/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<InvoiceDto> createInvoice(@PathVariable Long companyId, @RequestBody InvoiceDto invoiceDto,
            @UserIdAuth final User user) {
        if (invoiceDto.getTotalPrice() == null ||
                invoiceDto.getInvoiceItems() == null || invoiceDto.getInvoiceItems().isEmpty()) {
            throw new IllegalStateException("Missing mandatory fields");
        }

        return ResponseEntity.ok(invoiceService.createUpdate(companyId, invoiceDto, user));
    }

    // better to put this in the quote controller?
    @GetMapping("/quote/{quoteId}")
    public ResponseEntity<InvoiceDto> createInvoiceFromQuote(@PathVariable Long companyId, @PathVariable Long quoteId,
            @UserIdAuth final User user) {
        return ResponseEntity.ok(invoiceService.createInvoiceFromQuote(companyId, quoteId, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable Long companyId, @PathVariable Long id,
            @RequestBody InvoiceDto invoiceDto, @UserIdAuth final User user) {
        return ResponseEntity.ok(invoiceService.createUpdate(companyId, invoiceDto, user));
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceDto>> getAllInvoices(@PathVariable Long companyId,
            @QueryParam("lazy") boolean lazy, Pageable pageable, @UserIdAuth final User user) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(companyId, lazy, pageable, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long companyId, @PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(companyId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long companyId, @PathVariable Long id) {
        invoiceService.deleteInvoice(companyId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/pdf", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long companyId, @PathVariable Long id) {
        byte[] pdfContent = invoiceService.downloadInvoicePdf(companyId, id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=invoice_" + id + ".pdf")
                .body(pdfContent);
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<InvoiceDto> sendInvoice(@PathVariable Long companyId, @PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.sendInvoice(companyId, id));
    }

    @GetMapping("/{id}/public-link")
    public ResponseEntity<String> getPublicInvoiceLink(@PathVariable Long companyId, @PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getPublicInvoiceLink(companyId, id));
    }
}

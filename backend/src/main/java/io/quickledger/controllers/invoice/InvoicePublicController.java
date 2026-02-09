package io.quickledger.controllers.invoice;

import io.quickledger.dto.invoice.InvoiceDto;
import io.quickledger.entities.TempToken;
import io.quickledger.exception.TokenValidationException;
import io.quickledger.services.InvoiceService;
import io.quickledger.services.TempTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;

import java.util.Map;

@RestController
@RequestMapping("/public/invoices")
public class InvoicePublicController {
    private static final Logger logger = LoggerFactory.getLogger(InvoicePublicController.class);
    private final TempTokenService tempTokenService;
    private final InvoiceService invoiceService;

    public InvoicePublicController(TempTokenService tempTokenService, InvoiceService invoiceService) {
        this.tempTokenService = tempTokenService;
        this.invoiceService = invoiceService;
    }

    @GetMapping("/view")
    public ResponseEntity<?> getInvoiceByToken(@RequestParam String token) {
        try {
            Map<String, Object> claims = tempTokenService.validateToken(token, TempToken.TokenType.INVOICE_TOKEN);
            Long claimInvoiceId = Long.valueOf(claims.getOrDefault("invoiceId", "-1").toString());
            Long claimCompanyId = Long.valueOf(claims.getOrDefault("companyId", "-1").toString());

            if (claimInvoiceId == -1 || claimCompanyId == -1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token claims.");
            }

            InvoiceDto invoiceDto = invoiceService.getInvoiceById(claimCompanyId, claimInvoiceId);
            return ResponseEntity.ok(invoiceDto);
        } catch (TokenValidationException e) {
            logger.error("Token validation error: ", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invoice not found");
        } catch (Exception e) {
            logger.error("Error retrieving invoice: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving invoice.");
        }
    }
}

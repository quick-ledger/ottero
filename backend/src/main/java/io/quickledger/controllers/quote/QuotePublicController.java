package io.quickledger.controllers.quote;

import io.quickledger.dto.quote.QuoteDto;
import io.quickledger.entities.quote.Quote;
import io.quickledger.entities.TempToken;
import io.quickledger.exception.TokenValidationException;
import io.quickledger.services.QuoteService;
import io.quickledger.services.TempTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;

import java.util.Map;

@RestController
@RequestMapping("/public/quotes")
public class QuotePublicController {
    private static final Logger logger = LoggerFactory.getLogger(QuotePublicController.class);
    private final TempTokenService tempTokenService;
    private final QuoteService quoteService;

    public QuotePublicController(TempTokenService tempTokenService, QuoteService quoteService) {
        this.tempTokenService = tempTokenService;
        this.quoteService = quoteService;
    }

    @GetMapping("/view")
    public ResponseEntity<?> getQuoteByToken(@RequestParam String token) {
        try {
            Map<String, Object> claims = tempTokenService.validateToken(token, TempToken.TokenType.QUOTE_TOKEN);
            Long claimQuoteId = Long.valueOf(claims.getOrDefault("quoteId", "-1").toString());

            if (claimQuoteId == -1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token claims.");
            }

            QuoteDto quoteDto = quoteService.getQuoteById(claimQuoteId);
            return ResponseEntity.ok(quoteDto);
        } catch (TokenValidationException e) {
            logger.error("Token validation error: ", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Quote not found");
        } catch (Exception e) {
            logger.error("Error retrieving quote: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving quote.");
        }
    }

    @PostMapping("/action")
    public ResponseEntity<?> actionQuote(@RequestParam String token, @RequestBody QuoteDto actionDto) {
        try {
            Map<String, Object> claims = tempTokenService.validateToken(token, TempToken.TokenType.QUOTE_TOKEN);
            Long claimQuoteId = Long.valueOf(claims.getOrDefault("quoteId", "-1").toString());
            Long claimCompanyId = Long.valueOf(claims.getOrDefault("companyId", "-1").toString());

            if (claimQuoteId == -1 || claimCompanyId == -1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token claims.");
            }

            // Validate status change
            if (actionDto.getStatus() != Quote.QuoteStatus.ACCEPTED
                    && actionDto.getStatus() != Quote.QuoteStatus.REJECTED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid status action.");
            }

            // We use the existing service method.
            // Note: It might require extra validation that the quote matches company, which
            // we have from token.
            quoteService.approveQuote(claimQuoteId, actionDto, claimCompanyId);

            return ResponseEntity.ok("Quote updated successfully");
        } catch (TokenValidationException e) {
            logger.error("Token validation error: ", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating quote: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating quote.");
        }
    }
}
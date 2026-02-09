package io.quickledger.controllers.quote;

import io.quickledger.dto.quote.QuoteDto;
import io.quickledger.dto.quote.QuoteItemDto;
import io.quickledger.dto.quote.QuoteTempTokenValidationRequestDto;
import io.quickledger.dto.quote.QuoteTempTokenValidationResponseDto;
import io.quickledger.dto.quote.QuoteAttachmentDto;
import io.quickledger.entities.quote.Quote;
import io.quickledger.entities.TempToken;
import io.quickledger.entities.User;
import io.quickledger.exception.TokenValidationException;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.QuoteService;
import io.quickledger.services.TempTokenService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/companies/{companyId}/quotes")
public class QuoteController {
    private static final String TOKEN_SUBJECT = "quote-approval";
    private static final long TOKEN_EXPIRATION_TIME_MILLIS = 1000 * 60 * 60 * 24 * 7 * 2; // 14 days

    private final QuoteService quoteService;
    private final TempTokenService tempTokenService;
    private static final Logger logger = LoggerFactory.getLogger(QuoteController.class);

    public QuoteController(QuoteService quoteService, TempTokenService tempTokenService) {
        this.quoteService = quoteService;
        this.tempTokenService = tempTokenService;
    }

    @GetMapping("/{qouteId}/clients/{clientId}/generate-token")
    public String generateToken(@PathVariable Long companyId, @PathVariable Long qouteId, @PathVariable Long clientId,
            @UserIdAuth final User user) {
        // TODO we should make sure this quote belongs to the company and user.
        logger.debug("Generating token for quote with Quote Id: {} and Client Id: {}", qouteId, clientId);
        Map<String, Object> claims = new HashMap<>();
        claims.put(TempTokenService.QuoteTokenClaims.CLIENT_ID.getClaimKey(), clientId);
        claims.put(TempTokenService.QuoteTokenClaims.COMPANY_ID.getClaimKey(), companyId);
        claims.put(TempTokenService.QuoteTokenClaims.USER_ID.getClaimKey(), user.getId());
        claims.put(TempTokenService.QuoteTokenClaims.QUOTE_ID.getClaimKey(), qouteId);
        String token = tempTokenService.generateToken(claims, TOKEN_EXPIRATION_TIME_MILLIS, TOKEN_SUBJECT,
                TempToken.TokenType.QUOTE_TOKEN);
        logger.debug("Token generated: {}", token);
        return token;
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateTokenAndRetrieveClaims(@RequestBody QuoteTempTokenValidationRequestDto body,
            @PathVariable Long companyId, @UserIdAuth final User user) {
        try {
            // Assuming the token contains clientId, companyId, and quoteId as claims
            // @RG: we have following all in token: clientId, companyId, quoteId
            Map<String, Object> claims = tempTokenService.validateToken(body.getToken(),
                    TempToken.TokenType.QUOTE_TOKEN);
            Long claimClientId = Long.valueOf(claims.getOrDefault("clientId", "-1").toString());
            Long claimCompanyId = Long.valueOf(claims.getOrDefault("companyId", "-1").toString());
            Long claimQuoteId = Long.valueOf(claims.getOrDefault("quoteId", "-1").toString());

            logger.debug("Token claims: clientId={}, companyId={}, quoteId={}", claimClientId, claimCompanyId,
                    claimQuoteId);
            // Check if the claims contain valid IDs
            if (claimClientId == -1 || claimCompanyId == -1 || claimQuoteId == -1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token claims.");
            }

            QuoteTempTokenValidationResponseDto tokenClaimsDto = new QuoteTempTokenValidationResponseDto(claimCompanyId,
                    claimClientId, claimQuoteId);
            return ResponseEntity.ok(tokenClaimsDto);
        } catch (TokenValidationException e) {
            logger.error("Token validation error: ", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing token.");
        }
    }

    @PostMapping
    public ResponseEntity<?> createQuote(@PathVariable Long companyId, @RequestBody QuoteDto quoteDto,
            @UserIdAuth final User user) {
        logger.info("Creating quote for company with id: {}", companyId);

        // Validate mandatory fields
        if (quoteDto.getTotalPrice() == null ||
                quoteDto.getQuoteItems() == null || quoteDto.getQuoteItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing mandatory fields");
        }

        // Create or update quote
        QuoteDto createdOrUpdatedQuote = quoteService.createOrUpdateQuote(quoteDto, companyId, user.getId());

        return ResponseEntity.ok(createdOrUpdatedQuote);
    }

    // this returns latest revision of all quotes (removing duplicates i.e. other
    // revisions)
    @GetMapping("/quotenumbers")
    public ResponseEntity<Page<QuoteDto>> getAllQuotesNumbers(@PathVariable Long companyId,
            @QueryParam("lazy") boolean lazy, Pageable pageable, @UserIdAuth final User user) {
        Page<QuoteDto> quotes = quoteService.getAllQuotesLatestRevision(companyId, lazy, pageable, user);
        return ResponseEntity.ok(quotes);
    }

    // Get all quotes with optional filter for all revisions
    @GetMapping
    public ResponseEntity<Page<QuoteDto>> getAllQuotes(@PathVariable Long companyId,
            @QueryParam("lazy") boolean lazy,
            @QueryParam("showAllRevisions") boolean showAllRevisions,
            Pageable pageable, @UserIdAuth final User user) {
        Page<QuoteDto> quotes;
        if (showAllRevisions) {
            quotes = quoteService.getAllQuotesAllRevisions(companyId, lazy, pageable, user);
        } else {
            quotes = quoteService.getAllQuotesLatestRevision(companyId, lazy, pageable, user);
        }
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadQuotePdf(@PathVariable Long companyId, @PathVariable Long id) {
        byte[] pdfBytes = quoteService.generateQuotePdf(companyId, id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=quote-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteDto> getQuoteById(@PathVariable Long companyId, @PathVariable Long id) {
        QuoteDto quoteDto = quoteService.getQuoteById(id);
        return ResponseEntity.ok(quoteDto);
    }

    @GetMapping("/{id}/public-link")
    public ResponseEntity<String> getPublicQuoteLink(@PathVariable Long companyId, @PathVariable Long id) {
        String link = quoteService.getPublicQuoteLink(companyId, id);
        return ResponseEntity.ok(link);
    }

    @PostMapping("/{id}/copy")
    public ResponseEntity<QuoteDto> copyQuote(@PathVariable Long companyId, @PathVariable Long id,
            @UserIdAuth final User user) {
        QuoteDto newQuote = quoteService.duplicateQuote(id, companyId, user.getId());
        return ResponseEntity.ok(newQuote);
    }

    @PostMapping("/{id}/revise")
    public ResponseEntity<QuoteDto> reviseQuote(@PathVariable Long companyId, @PathVariable Long id,
            @UserIdAuth final User user) {
        QuoteDto newQuote = quoteService.reviseQuote(id, companyId, user.getId());
        return ResponseEntity.ok(newQuote);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuoteDto> updateQuote(@PathVariable Long companyId, @PathVariable Long id,
            @RequestBody QuoteDto quoteDto, @UserIdAuth final User user) {
        quoteDto.setId(id);
        QuoteDto updatedQuote = quoteService.createOrUpdateQuote(quoteDto, companyId, user.getId());
        return ResponseEntity.ok(updatedQuote);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long companyId, @PathVariable Long id) {
        quoteService.deleteQuote(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approval")
    public ResponseEntity<?> approveQuote(@PathVariable Long companyId, @PathVariable Long id,
            @RequestBody QuoteDto quoteDto) {
        try {
            // check to make sure that quoteDto's status is either ACCEPTED or REJECTED
            if (quoteDto.getStatus() != Quote.QuoteStatus.ACCEPTED
                    && quoteDto.getStatus() != Quote.QuoteStatus.REJECTED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid status. Status must be either ACCEPTED or REJECTED.");
            }
            quoteService.approveQuote(id, quoteDto, companyId);
            return ResponseEntity.ok("Quote approval status updated successfully.");
        } catch (EntityNotFoundException e) {
            logger.error("Quote not found: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating quote approval status: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating quote approval status.");
        }
    }

    @PostMapping("/{quoteId}/items")
    public ResponseEntity<QuoteItemDto> createOrUpdateQuoteItem(@PathVariable Long companyId,
            @PathVariable Long quoteId, @RequestBody QuoteItemDto quoteItemDto) {
        QuoteItemDto createdQuoteItem = quoteService.createOrUpdateQuoteItem(quoteId, quoteItemDto);
        return ResponseEntity.ok(createdQuoteItem);
    }

    @GetMapping("/{quoteId}/items")
    public ResponseEntity<List<QuoteItemDto>> getAllQuoteItems(@PathVariable Long companyId,
            @PathVariable Long quoteId) {
        List<QuoteItemDto> quoteItems = quoteService.getAllQuoteItemsByQuoteId(quoteId);
        return ResponseEntity.ok(quoteItems);
    }

    // TODO send the quote to the client. after this the quote is not editable.
    // email or sms.
    @PostMapping("/{quoteId}/send-quote")
    public QuoteDto sendQuote(@PathVariable Long quoteId) {
        return quoteService.sendQuote(quoteId);
    }

    // specific revision of a quote number
    @GetMapping("/quotenumbers/{quoteNumber}/revisions/{quoteRevision}")
    public ResponseEntity<QuoteDto> getQuoteByQuoteNumberAndRevision(@PathVariable Long companyId,
            @PathVariable String quoteNumber, @PathVariable Integer quoteRevision) {
        Optional<QuoteDto> quote = quoteService.findQuoteByQuoteNumberAndRevision(companyId, quoteNumber,
                quoteRevision);
        return quote.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // all revisions of a quote number
    @GetMapping("/quotenumbers/{quoteNumber}/revisions")
    public ResponseEntity<List<QuoteDto>> getAllRevisionsByQuoteNumber(@PathVariable Long companyId,
            @PathVariable String quoteNumber, @QueryParam("lazy") boolean lazy) {
        List<QuoteDto> allRevisions = quoteService.findAllRevisionsByQuoteNumber(companyId, quoteNumber, lazy);
        return ResponseEntity.ok(allRevisions);
    }

    // latest revision of a quote number
    @GetMapping("/quotenumbers/{quoteNumber}/revisions/latest")
    public ResponseEntity<QuoteDto> getLatestRevisionByQuoteNumber(@PathVariable Long companyId,
            @PathVariable String quoteNumber) {
        Optional<QuoteDto> quote = quoteService.findLatestRevision(quoteNumber, companyId);
        return quote.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // search quote by quoteNumber and client name
    @GetMapping("/search")
    public ResponseEntity<List<QuoteDto>> searchQuote(@PathVariable Long companyId,
            @RequestParam(required = true) String searchTerm, @QueryParam("lazy") boolean lazy) {
        List<QuoteDto> quotes = quoteService.searchQuote(companyId, searchTerm, lazy);
        return ResponseEntity.ok(quotes);
    }

    @PostMapping("/{quoteId}/attachments")
    public ResponseEntity<QuoteAttachmentDto> uploadAttachment(
            @PathVariable Long companyId,
            @PathVariable Long quoteId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            QuoteAttachmentDto attachment = quoteService.uploadAttachment(
                    quoteId,
                    companyId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes(),
                    file.getSize());
            return ResponseEntity.ok(attachment);
        } catch (java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{quoteId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable Long companyId,
            @PathVariable Long quoteId,
            @PathVariable Long attachmentId) {
        io.quickledger.entities.quote.QuoteAttachment attachment = quoteService.getAttachmentEntity(attachmentId,
                companyId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .body(attachment.getData());
    }

    @DeleteMapping("/{quoteId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long companyId,
            @PathVariable Long quoteId,
            @PathVariable Long attachmentId) {
        quoteService.deleteAttachment(attachmentId, companyId);
        return ResponseEntity.noContent().build();
    }
}
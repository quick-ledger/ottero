package io.quickledger.services;

import io.quickledger.dto.ClientDto;
import io.quickledger.dto.quote.QuoteDto;
import io.quickledger.dto.quote.QuoteItemDto;
import java.math.BigDecimal;
import io.quickledger.entities.*;
import io.quickledger.entities.quote.Quote;
import io.quickledger.entities.quote.QuoteItem;
import io.quickledger.mappers.ClientMapper;
import io.quickledger.mappers.quote.QuoteMapper;
import io.quickledger.mappers.quote.QuoteItemMapper;
import io.quickledger.repositories.*;
import io.quickledger.repositories.quote.QuoteItemRepository;
import io.quickledger.repositories.quote.QuoteRepository;
import io.quickledger.repositories.quote.QuoteAttachmentRepository;
import io.quickledger.entities.quote.QuoteAttachment;
import io.quickledger.dto.quote.QuoteAttachmentDto;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class QuoteService {

    private static final Logger logger = LoggerFactory.getLogger(QuoteService.class);
    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final QuoteMapper quoteMapper;
    private final ClientMapper clientMapper;
    private final QuoteItemMapper quoteItemMapper;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final SequenceConfigRepository sequenceConfigRepository;
    private final ClientService clientService;
    private final TempTokenService tempTokenService;
    private final PdfService pdfService;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final QuoteAttachmentRepository quoteAttachmentRepository;
    private final io.quickledger.mappers.quote.QuoteAttachmentMapper quoteAttachmentMapper;
    private final UserCompanyService userCompanyService;
    private final String appBaseUrl;

    public QuoteService(QuoteRepository quoteRepository, QuoteItemRepository quoteItemRepository,
            QuoteMapper quoteMapper,
            QuoteItemMapper quoteItemMapper, ClientRepository clientRepository, UserRepository userRepository,
            SequenceConfigRepository sequenceConfigRepository, ClientService clientService, ClientMapper clientMapper,
            TempTokenService tempTokenService, PdfService pdfService,
            QuoteAttachmentRepository quoteAttachmentRepository,
            io.quickledger.mappers.quote.QuoteAttachmentMapper quoteAttachmentMapper,
            ObjectMapper objectMapper, EmailService emailService,
            UserCompanyService userCompanyService,
            @Value("${application.frontend.url}") String appBaseUrl) {
        this.quoteRepository = quoteRepository;
        this.quoteItemRepository = quoteItemRepository;
        this.quoteMapper = quoteMapper;
        this.quoteItemMapper = quoteItemMapper;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.sequenceConfigRepository = sequenceConfigRepository;
        this.clientService = clientService;
        this.clientMapper = clientMapper;
        this.tempTokenService = tempTokenService;
        this.pdfService = pdfService;
        this.quoteAttachmentRepository = quoteAttachmentRepository;
        this.quoteAttachmentMapper = quoteAttachmentMapper;
        this.objectMapper = objectMapper;
        this.emailService = emailService;
        this.userCompanyService = userCompanyService;
        this.appBaseUrl = appBaseUrl;
    }

    @Transactional
    public QuoteDto sendQuote(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found with id: " + quoteId));

        if (quote.getClient() == null || StringUtils.isBlank(quote.getClient().getEmail())) {
            throw new IllegalStateException("Client email not found for quote: " + quoteId);
        }

        // Generate PDF
        byte[] pdfBytes = generateQuotePdf(quote.getCompany().getId(), quoteId);

        // Generate Public Token (30 Days Validity)
        Map<String, Object> claims = new HashMap<>();
        claims.put("clientId", quote.getClient().getId());
        claims.put("companyId", quote.getCompany().getId());
        claims.put("quoteId", quote.getId());

        long expiryMillis = 30L * 24 * 60 * 60 * 1000; // 30 Days
        String token = tempTokenService.generateToken(claims, expiryMillis, quote.getClient().getEmail(),
                TempToken.TokenType.QUOTE_TOKEN);

        String publicLink = appBaseUrl + "/public/quotes/" + quote.getId() + "?token=" + token;

        String emailBody = "Dear " + quote.getClient().getContactName() + ",\n\n" +
                "Please find attached your quote #" + quote.getQuoteNumber() + ".\n\n" +
                "You can view, approve, or reject this quote online at:\n" +
                publicLink + "\n\n" +
                "Total Amount: " + quote.getTotalPrice() + "\n\n" +
                "Best regards,\n" +
                quote.getCompany().getName();

        String pdfFileName = "quote-" + quote.getQuoteNumber() + ".pdf";

        emailService.sendEmailWithAttachment(
                quote.getClient().getEmail(),
                "Quote #" + quote.getQuoteNumber(),
                emailBody,
                pdfBytes,
                pdfFileName,
                quote.getCompany().getName(),
                quote.getCompany().getEmail(),
                false);

        quote.setStatus(Quote.QuoteStatus.SENT);
        logger.info("Quote {} status updated to SENT", quote.getId());
        quoteRepository.save(quote);
        return quoteMapper.toDto(quote, false);
    }

    public String getPublicQuoteLink(Long companyId, Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found with id: " + quoteId));

        if (!quote.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Quote does not belong to company: " + companyId);
        }

        if (quote.getClient() == null) {
            throw new IllegalStateException("Quote does not have a client assigned.");
        }

        // Generate Public Token (30 Days Validity)
        Map<String, Object> claims = new HashMap<>();
        claims.put("clientId", quote.getClient().getId());
        claims.put("companyId", quote.getCompany().getId());
        claims.put("quoteId", quote.getId());

        long expiryMillis = 30L * 24 * 60 * 60 * 1000; // 30 Days
        String token = tempTokenService.generateToken(claims, expiryMillis, quote.getClient().getEmail(),
                TempToken.TokenType.QUOTE_TOKEN);

        return appBaseUrl + "/public/quotes/" + quote.getId() + "?token=" + token;
    }

    @Transactional
    public List<QuoteDto> findAllRevisionsByQuoteNumber(Long companyId, String quoteNumber, boolean lazy) {
        List<Quote> allRevisions = quoteRepository.findByQuoteNumberAndCompanyId(quoteNumber, companyId);
        return allRevisions.stream()
                .map(quote -> quoteMapper.toDto(quote, lazy))
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<QuoteDto> findQuoteByQuoteNumberAndRevision(Long companyId, String quoteNumber,
            Integer quoteRevision) {
        Optional<Quote> quote = quoteRepository.findByQuoteNumberAndQuoteRevisionAndCompanyId(quoteNumber,
                quoteRevision, companyId);
        return quote.map(q -> quoteMapper.toDto(q, false));
    }

    @Transactional
    public void approveQuote(Long quoteId, QuoteDto quoteDto, Long companyId) {
        logger.debug("Approving quote with id: {}", quoteId);
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found with quoteId " + quoteId));

        if (quoteDto.getStatus() != null) {
            quote.setStatus(quoteDto.getStatus());
        }

        quote.setClientNotes(quoteDto.getClientNotes());

        // Invalidate the token no matter what is the status
        // Only invalidate if client exists to prevent NullPointerException
        if (quote.getClient() != null) {
            tempTokenService.invalidateTokenByClientIdAndCompanyIdAndTokenType(
                    quote.getClient().getId(),
                    companyId,
                    TempToken.TokenType.QUOTE_TOKEN);
        } else {
            logger.warn("Quote {} has no associated client, skipping token invalidation", quoteId);
        }

        quoteRepository.save(quote);

        // Send email notification to company/user about the status change
        try {
            String toEmail = quote.getUser() != null ? quote.getUser().getEmail() : quote.getCompany().getEmail();
            if (StringUtils.isNotBlank(toEmail)) {
                String subject = "Quote " + quoteDto.getStatus() + ": #" + quote.getQuoteNumber();
                String body = "Quote #" + quote.getQuoteNumber() + " has been " + quoteDto.getStatus()
                        + " by " +
                        quote.getClient().getContactName() + " " + quote.getClient().getContactSurname() + ".\n\n" +
                        "Client Notes: " + (quoteDto.getClientNotes() != null ? quoteDto.getClientNotes() : "N/A")
                        + "\n\n" +
                        "Link: " + appBaseUrl + "/quotes/" + quote.getId();
                emailService.sendEmail(toEmail, subject, body);
            } else {
                logger.warn("No email found for company/user to send quote approval notification. QuoteId: {}",
                        quoteId);
            }
        } catch (Exception e) {
            logger.error("Failed to send quote approval notification email", e);
            // Don't fail the transaction just because email failed
        }
    }

    public Page<QuoteDto> getAllQuotesLatestRevision(Long companyId, boolean lazy, Pageable pageable, User user) {
        userCompanyService.checkUserBelongs(user, companyId);
        Page<Quote> quotes = quoteRepository.getAllQuotesLatestRevision(companyId, pageable);
        return quotes.map(quote -> quoteMapper.toDto(quote, lazy));
    }

    public Page<QuoteDto> getAllQuotesAllRevisions(Long companyId, boolean lazy, Pageable pageable, User user) {
        userCompanyService.checkUserBelongs(user, companyId);
        Page<Quote> quotes = quoteRepository.findByCompanyId(companyId, pageable);
        return quotes.map(quote -> quoteMapper.toDto(quote, lazy));
    }

    public List<QuoteDto> searchQuote(Long companyId, String searchTerm, boolean lazy) {
        List<Quote> quote = quoteRepository.searchQuote(companyId, searchTerm);
        return quote.stream().map(q -> quoteMapper.toDto(q, lazy)).collect(Collectors.toList());
    }

    // MBH I set this up to handle both create and update operations as we only need
    // sequence generation for Create not update.
    public enum Operation {
        CREATE, UPDATE
    }

    @Transactional
    public QuoteDto createOrUpdateQuote(QuoteDto quoteDto, Long companyId, Long userid) {
        Quote quote;

        if (quoteDto.getId() != null) {
            // Update scenario: Fetch existing quote to ensure we don't wipe unmapped fields
            // (like attachments)
            quote = quoteRepository.findById(quoteDto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Quote not found with id: " + quoteDto.getId()));

            // Prevent modification of finalised quotes
            if (quote.getStatus() == Quote.QuoteStatus.ACCEPTED ||
                    quote.getStatus() == Quote.QuoteStatus.REJECTED ||
                    quote.getStatus() == Quote.QuoteStatus.CANCELLED ||
                    quote.getStatus() == Quote.QuoteStatus.SENT) {
                throw new IllegalStateException("Cannot modify a quote that is " + quote.getStatus()
                        + ". Please create a new revision instead.");
            }

            // Map DTO fields onto the existing entity
            quoteMapper.updateEntityFromDto(quoteDto, quote);

        } else {
            // Create scenario (New Quote or New Revision)
            quote = quoteMapper.toEntity(quoteDto);

            // Check if this is a new revision of an existing quote number
            if (!StringUtils.isBlank(quote.getQuoteNumber()) && !Quote.QuoteStatus.PENDING.equals(quote.getStatus())) {
                // Check revNumber on dto is the latest.
                Optional<Quote> oldQuote = quoteRepository
                        .findLatestRevisionByQuoteNumberAndCompanyId(quote.getQuoteNumber(), companyId);
                if (oldQuote.isPresent())
                    quote.setQuoteRevision(oldQuote.get().getQuoteRevision() + 1);
            } else if (StringUtils.isBlank(quote.getQuoteNumber())) {
                // Completely new quote

                // Enforce Plan Limits (Free & Basic = 5 quotes/month)
                if (userid != null) {
                    User user = userRepository.findById(userid).orElse(null);
                    if (user != null) {
                        String plan = user.getSubscriptionPlan();
                        if (plan == null || "Free".equalsIgnoreCase(plan) || "Basic".equalsIgnoreCase(plan)) {
                            long count = quoteRepository.countMonthlyQuotesByCompanyId(companyId);
                            if (count >= 5) {
                                throw new IllegalStateException(
                                        "You have reached the limit of 5 quotes per month on the "
                                                + (plan == null ? "Free" : plan) + " plan.");
                            }
                        }
                    }
                }

                String quoteNumber = generateQuoteSequence(quoteDto.getCompanyId());
                quote.setQuoteNumber(quoteNumber);
                quote.setQuoteRevision(0); // Initialize first revision as 0

                // Default dates if missing
                if (quote.getQuoteDate() == null) {
                    quote.setQuoteDate(java.time.LocalDate.now());
                }
                if (quote.getExpiryDate() == null) {
                    quote.setExpiryDate(java.time.LocalDate.now().plusDays(14));
                }
            }
        }

        setClient(quoteDto, companyId, quote);

        if (userid != null) {
            User user = userRepository.findById(userid)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + quoteDto.getUserId()));
            quote.setUser(user);
        }

        // Quote Items Update
        if (quote.getQuoteItems() != null) {
            for (QuoteItem quoteItem : quote.getQuoteItems()) {
                quoteItem.setQuote(quote);
            }
        }

        // Backend Calculation of Totals and GST to ensure data integrity
        calculateQuoteTotals(quote);

        quote = quoteRepository.save(quote);
        return quoteMapper.toDto(quote, false);
    }

    private void calculateQuoteTotals(Quote quote) {
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;

        if (quote.getQuoteItems() != null) {
            for (QuoteItem item : quote.getQuoteItems()) {
                BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                BigDecimal gstRateVal = item.getGst() != null ? item.getGst() : BigDecimal.ZERO;

                BigDecimal lineTotalExGst = qty.multiply(price);
                BigDecimal lineTotalIncGst = lineTotalExGst;
                BigDecimal lineGst = BigDecimal.ZERO;

                // Assuming GST is 10% if set to 10. Logic mirrors frontend: price * 1.1
                // Check if gst is essentially 10 (comparing 10.00, 10, etc)
                if (gstRateVal.compareTo(BigDecimal.ZERO) > 0) {
                    // Using 1.1 multiplier for Australian GST (10%)
                    // If the rate is exactly 10, we multiply by 1.1.
                    // If we want to support dynamic rates, we'd use (1 + rate/100).
                    // Frontend implies strict 10% or 0%.
                    // Let's use dynamic standard formula: total = price * (1 + rate/100)

                    BigDecimal multiplier = BigDecimal.ONE.add(gstRateVal.divide(BigDecimal.valueOf(100)));
                    lineTotalIncGst = lineTotalExGst.multiply(multiplier);
                    lineGst = lineTotalIncGst.subtract(lineTotalExGst);
                }

                item.setTotal(lineTotalIncGst);
                subTotal = subTotal.add(lineTotalIncGst);
                totalGst = totalGst.add(lineGst);
            }
        }

        // Apply Discount
        BigDecimal finalTotal = subTotal;
        BigDecimal discountVal = quote.getDiscountValue() != null ? quote.getDiscountValue() : BigDecimal.ZERO;

        if (quote.getDiscountType() != null && "DOLLAR".equals(quote.getDiscountType())) {
            finalTotal = finalTotal.subtract(discountVal);
        } else if (quote.getDiscountType() != null && "PERCENT".equals(quote.getDiscountType())) {
            // percentage off the total
            BigDecimal discountAmount = finalTotal.multiply(discountVal).divide(BigDecimal.valueOf(100));
            finalTotal = finalTotal.subtract(discountAmount);
        }

        quote.setTotalPrice(finalTotal);
        quote.setGst(totalGst);
    }

    private void setClient(QuoteDto quoteDto, Long companyId, Quote quote) {
        if (quoteDto.getClientId() != null) {
            Client client = clientRepository.findById(quoteDto.getClientId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Client not found with id: " + quoteDto.getClientId()));
            quote.setClient(client);
        } else {
            // Try to find existing client by email or phone
            Optional<Client> existingClient = Optional.empty();

            if (StringUtils.isNotBlank(quoteDto.getClientEmail())) {
                existingClient = clientRepository.findByEmailAndCompanyId(quoteDto.getClientEmail(), companyId);
            }

            if (!existingClient.isPresent() && StringUtils.isNotBlank(quoteDto.getClientPhone())) {
                existingClient = clientRepository.findByPhoneAndCompanyId(quoteDto.getClientPhone(), companyId);
            }

            if (existingClient.isPresent()) {
                Client client = existingClient.get();
                // Optionally update client details here if needed, but for now just link them
                quote.setClient(client);
            } else {
                ClientDto clientDto = new ClientDto();
                clientDto.setId(quoteDto.getClientId());
                clientDto.setEmail(quoteDto.getClientEmail());
                clientDto.setPhone(quoteDto.getClientPhone());
                clientDto.setCompanyId(quoteDto.getCompanyId());
                clientDto.setFirstName(quoteDto.getClientFirstname());
                clientDto.setLastName(quoteDto.getClientLastname());
                clientDto.setEntityName(quoteDto.getClientEntityName());

                logger.debug("Creating new client for quote: {}", clientDto);
                clientDto = clientService.saveClient(clientDto, companyId);

                quote.setClient(clientMapper.toEntity(clientDto));
            }
        }
    }

    // TODO return quote items sorted by created date (from base entitiy)
    public List<QuoteDto> getAllQuotes(Long companyId, boolean lazy) {
        List<Quote> quotes = quoteRepository.findByCompanyId(companyId);
        return quotes.stream().map(quote -> quoteMapper.toDto(quote, lazy)).collect(Collectors.toList());
    }

    public QuoteDto getQuoteById(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found with id " + id));
        return quoteMapper.toDto(quote, false);
    }

    @Transactional(readOnly = true)
    public byte[] generateQuotePdf(Long companyId, Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found with id: " + quoteId));

        if (!quote.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Quote does not belong to company: " + companyId);
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("quote", quote);
        variables.put("company", quote.getCompany());

        // Process Company Template Config
        Company company = quote.getCompany();
        if (company.getTemplateConfig() != null && !company.getTemplateConfig().isEmpty()) {
            try {
                JsonNode config = objectMapper.readTree(company.getTemplateConfig());
                if (config.has("notes")) {
                    variables.put("paymentTerms", config.get("notes").asText());
                }
                if (config.has("logoWidth")) {
                    variables.put("logoWidth", config.get("logoWidth").asText());
                }
                if (config.has("logoHeight")) {
                    variables.put("logoHeight", config.get("logoHeight").asText());
                }
                if (config.has("logoPosition")) {
                    variables.put("logoPosition", config.get("logoPosition").asText());
                }
            } catch (Exception e) {
                logger.error("Failed to parse template config for company " + companyId, e);
            }
        }
        if (company.getImage() != null && company.getImage().length > 0) {
            String logoBase64 = java.util.Base64.getEncoder().encodeToString(company.getImage());
            variables.put("companyLogoBase64", logoBase64);

            // Use content type if available, otherwise default to image/png
            String contentType = company.getLogoContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/png";
            }
            variables.put("companyLogoContentType", contentType);
        }

        return pdfService.generatePdfFromHtml("quote_pdf", variables);
    }

    public void deleteQuote(Long id) {
        quoteRepository.deleteById(id);
    }

    @Transactional
    public QuoteItemDto createOrUpdateQuoteItem(Long quoteId, QuoteItemDto quoteItemDto) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found with id: " + quoteId));

        QuoteItem quoteItem;
        if (quoteItemDto.getId() != null) {
            quoteItem = quoteItemRepository.findById(quoteItemDto.getId())
                    .orElse(new QuoteItem());
        } else {
            quoteItem = new QuoteItem();
        }

        quoteItemMapper.updateEntityFromDto(quoteItemDto, quoteItem);
        quoteItem.setQuote(quote);
        quoteItem = quoteItemRepository.save(quoteItem);

        return quoteItemMapper.toDto(quoteItem);
    }

    @Transactional
    public String generateQuoteSequence(Long companyId) {
        // Fetch the SequenceConfig with pessimistic lock to prevent race conditions
        Optional<SequenceConfig> sequenceConfigOptional = sequenceConfigRepository
                .findByEntityTypeAndCompanyIdForUpdate(SequenceConfig.EntityType.QUOTE, companyId);

        if (!sequenceConfigOptional.isPresent()) {
            throw new IllegalStateException("Sequence configuration not found for QUOTE and company ID: "
                    + companyId + " please configure the sequence configuration for your Quote");
        }

        SequenceConfig sequenceConfig = sequenceConfigOptional.get();
        Integer currentNumber = sequenceConfig.getCurrentNumber();

        int newQuoteSequence = currentNumber + 1;

        // Generate the new quote number with date placeholders and padding
        String newQuoteNumber = io.quickledger.utils.SequenceNumberFormatter.format(
                sequenceConfig.getPrefix(),
                newQuoteSequence,
                sequenceConfig.getPostfix(),
                sequenceConfig.getNumberPadding(),
                java.time.LocalDate.now());

        // Update the current number in the database
        sequenceConfig.setCurrentNumber(newQuoteSequence);
        sequenceConfigRepository.save(sequenceConfig);

        return newQuoteNumber;
    }

    @Transactional
    public List<QuoteItemDto> getAllQuoteItemsByQuoteId(Long quoteId) {
        // Check if the Quote exists
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found with id: " + quoteId));

        // Fetch all QuoteItems associated with the Quote
        List<QuoteItem> quoteItems = quoteItemRepository.findAllByQuote(quote);

        // Convert each QuoteItem to QuoteItemDto
        return quoteItems.stream()
                .map(quoteItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<QuoteDto> findLatestRevision(String quoteNumber, Long companyId) {
        Optional<Quote> out = quoteRepository.findLatestRevisionByQuoteNumberAndCompanyId(quoteNumber, companyId);
        return out.map(q -> quoteMapper.toDto(q, false));
    }

    @Transactional
    public QuoteAttachmentDto uploadAttachment(Long quoteId, Long companyId, String fileName, String contentType,
            byte[] data, long size) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found with id: " + quoteId));

        if (!quote.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Quote does not belong to company: " + companyId);
        }

        QuoteAttachment attachment = new QuoteAttachment();
        attachment.setQuote(quote);
        attachment.setCompany(quote.getCompany());
        attachment.setFileName(fileName);
        attachment.setContentType(contentType);
        attachment.setData(data);
        attachment.setSize(size);

        attachment = quoteAttachmentRepository.save(attachment);
        return quoteAttachmentMapper.toDto(attachment);
    }

    @Transactional(readOnly = true)
    public QuoteAttachment getAttachmentEntity(Long attachmentId, Long companyId) {
        return quoteAttachmentRepository.findByIdAndCompanyId(attachmentId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + attachmentId));
    }

    @Transactional
    public void deleteAttachment(Long attachmentId, Long companyId) {
        QuoteAttachment attachment = quoteAttachmentRepository.findByIdAndCompanyId(attachmentId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + attachmentId));
        quoteAttachmentRepository.delete(attachment);
    }

    @Transactional
    public QuoteDto duplicateQuote(Long quoteId, Long companyId, Long userId) {
        // Fetch the existing quote
        Quote existingQuote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found with id: " + quoteId));

        // Ensure it belongs to the company
        if (!existingQuote.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Quote does not belong to company: " + companyId);
        }

        // Map to DTO
        QuoteDto quoteDto = quoteMapper.toDto(existingQuote, false);

        // Reset fields for new quote
        quoteDto.setId(null);
        quoteDto.setQuoteNumber(null);
        quoteDto.setStatus(Quote.QuoteStatus.PENDING);
        quoteDto.setClientNotes(null);
        quoteDto.setQuoteRevision(0);

        // Reset item IDs
        if (quoteDto.getQuoteItems() != null) {
            quoteDto.getQuoteItems().forEach(item -> item.setId(null));
        }

        // Create new quote using existing creation logic
        return createOrUpdateQuote(quoteDto, companyId, userId);
    }

    @Transactional
    public QuoteDto reviseQuote(Long quoteId, Long companyId, Long userId) {
        // Fetch the existing quote (READ-ONLY - original will remain intact)
        Quote existingQuote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found with id: " + quoteId));

        // Ensure it belongs to the company
        if (!existingQuote.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Quote does not belong to company: " + companyId);
        }

        // Find LATEST revision for this quote number to ensure we increment from the
        // top
        Optional<Quote> latestReviewOpt = quoteRepository
                .findLatestRevisionByQuoteNumberAndCompanyId(existingQuote.getQuoteNumber(), companyId);
        int nextRevision = latestReviewOpt.map(q -> q.getQuoteRevision() + 1)
                .orElse(existingQuote.getQuoteRevision() + 1);

        // Convert existing quote to DTO (creates a deep copy of data)
        QuoteDto quoteDto = quoteMapper.toDto(existingQuote, false);

        // Prepare new revision by resetting identifiers
        quoteDto.setId(null); // Critical: null ID ensures a NEW entity is created
        quoteDto.setQuoteNumber(existingQuote.getQuoteNumber()); // KEEP same quote number
        quoteDto.setQuoteRevision(nextRevision); // Increment revision
        quoteDto.setStatus(Quote.QuoteStatus.PENDING); // Reset to PENDING for new revision
        quoteDto.setClientNotes(null); // Clear client notes for new revision

        // Reset quote item IDs to create new copies (not update existing)
        if (quoteDto.getQuoteItems() != null) {
            quoteDto.getQuoteItems().forEach(item -> item.setId(null));
        }

        // Create new quote revision (original quote remains unchanged in database)
        return createOrUpdateQuote(quoteDto, companyId, userId);
    }
}
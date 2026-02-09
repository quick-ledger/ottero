package io.quickledger.services;

import java.math.BigDecimal;
import io.quickledger.dto.ClientDto;
import io.quickledger.dto.invoice.InvoiceDto;
import io.quickledger.entities.Client;
import io.quickledger.entities.User;
import io.quickledger.entities.Company;
import io.quickledger.entities.SequenceConfig;
import io.quickledger.entities.invoice.Invoice;
import io.quickledger.entities.invoice.InvoiceItem;
import io.quickledger.entities.quote.Quote;
import io.quickledger.entities.quote.QuoteItem;
import io.quickledger.mappers.ClientMapper;
import io.quickledger.mappers.invoice.InvoiceItemMapper;
import io.quickledger.mappers.invoice.InvoiceMapper;
import io.quickledger.repositories.ClientRepository;
import io.quickledger.repositories.InvoiceRepository;
import io.quickledger.repositories.SequenceConfigRepository;
import io.quickledger.repositories.quote.QuoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Transactional
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
    private final ClientRepository clientRepository;
    private final UserCompanyService userCompanyService;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceItemMapper invoiceItemMapper;
    private final ClientMapper clientMapper;

    private final ClientService clientService;

    private final QuoteRepository quoteRepository;
    private final SequenceConfigRepository sequenceConfigRepository;
    private final PdfService pdfService;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final StripeService stripeService;
    private final TempTokenService tempTokenService;

    @org.springframework.beans.factory.annotation.Value("${application.frontend.url}")
    private String applicationFrontendUrl;

    public InvoiceService(InvoiceRepository invoiceRepository, ClientService clientService,
            ClientRepository clientRepository,
            UserCompanyService userCompanyService,
            InvoiceMapper invoiceMapper, ClientMapper clientMapper, InvoiceItemMapper invoiceItemMapper,
            QuoteRepository quoteRepository, SequenceConfigRepository sequenceConfigRepository,
            PdfService pdfService, ObjectMapper objectMapper, EmailService emailService, StripeService stripeService,
            TempTokenService tempTokenService) {
        this.invoiceRepository = invoiceRepository;
        this.clientService = clientService;
        this.clientRepository = clientRepository;
        this.userCompanyService = userCompanyService;
        this.invoiceMapper = invoiceMapper;
        this.clientMapper = clientMapper;
        this.invoiceItemMapper = invoiceItemMapper;
        this.quoteRepository = quoteRepository;
        this.sequenceConfigRepository = sequenceConfigRepository;
        this.pdfService = pdfService;
        this.objectMapper = objectMapper;
        this.emailService = emailService;
        this.stripeService = stripeService;
        this.tempTokenService = tempTokenService;
    }

    public InvoiceDto createUpdate(Long companyId, InvoiceDto invoiceDto, final User user) {
        logger.info("Creating invoice for company with id: {}", companyId);

        if (invoiceDto.getId() != null) {
            invoiceRepository.findById(invoiceDto.getId()).ifPresent(existingInvoice -> {
                if (existingInvoice.getStatus() == Invoice.InvoiceStatus.PAID ||
                        existingInvoice.getStatus() == Invoice.InvoiceStatus.SENT ||
                        existingInvoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
                    throw new IllegalStateException(
                            "Invoices that are " + existingInvoice.getStatus() + " cannot be modified");
                }
            });
        }

        Invoice invoice = invoiceMapper.toEntity(invoiceDto);

        // this is a new invoice
        if (invoice.getId() == null && StringUtils.isBlank(invoice.getInvoiceNumber())) {

            // Enforce Plan Limits (Free & Basic = 5 invoices/month)
            String plan = user.getSubscriptionPlan();
            if (plan == null || "Free".equalsIgnoreCase(plan) || "Basic".equalsIgnoreCase(plan)) {
                long count = invoiceRepository.countMonthlyInvoicesByCompanyId(companyId);
                if (count >= 5) {
                    throw new IllegalStateException("You have reached the limit of 5 invoices per month on the "
                            + (plan == null ? "Free" : plan) + " plan.");
                }
            }

            invoice.setInvoiceNumber(generateInvoiceSequence(invoiceDto.getCompanyId()));

            // Default dates if missing
            if (invoice.getInvoiceDate() == null) {
                invoice.setInvoiceDate(java.time.LocalDate.now().toString());
            }
            if (invoice.getDueDate() == null) {
                invoice.setDueDate(java.time.LocalDate.now().plusDays(14).toString());
            }
        }

        setClient(invoiceDto, companyId, invoice);
        userCompanyService.checkUserBelongs(user, companyId);
        invoice.setUser(user);

        // Set the invoice reference on each invoice item and clear transient entity
        // references
        if (invoice.getInvoiceItems() != null)
            for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                invoiceItem.setInvoice(invoice);
                // Clear transient references to avoid "unsaved transient instance" error
                invoiceItem.setServiceItem(null);
                invoiceItem.setProductItem(null);
            }

        calculateInvoiceTotals(invoice);

        invoice = invoiceRepository.save(invoice);
        InvoiceDto savedInvoiceDto = invoiceMapper.toDto(invoice, false);
        savedInvoiceDto.setPaymentLink(generatePaymentLink(invoice));
        return savedInvoiceDto;
    }

    public InvoiceDto createInvoiceFromQuote(Long companyId, Long quoteId, User user) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found with id: " + quoteId));

        if (!quote.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Quote company does not match");
        }

        Invoice invoice = new Invoice();
        invoice.setCompany(quote.getCompany());
        invoice.setClient(quote.getClient());
        if (invoice.getClient() == null) {
            throw new IllegalStateException(
                    "Cannot convert quote #" + quote.getQuoteNumber() + " to invoice: No client assigned to quote.");
        }
        invoice.setQuote(quote);
        invoice.setUser(user);

        // Enforce Plan Limits (Free & Basic = 5 invoices/month)
        String plan = user.getSubscriptionPlan();
        if (plan == null || "Free".equalsIgnoreCase(plan) || "Basic".equalsIgnoreCase(plan)) {
            long count = invoiceRepository.countMonthlyInvoicesByCompanyId(companyId);
            if (count >= 5) {
                throw new IllegalStateException("You have reached the limit of 5 invoices per month on the "
                        + (plan == null ? "Free" : plan) + " plan.");
            }
        }

        invoice.setInvoiceNumber(generateInvoiceSequence(companyId));

        // Default dates
        invoice.setInvoiceDate(java.time.LocalDate.now().toString());
        invoice.setDueDate(java.time.LocalDate.now().plusDays(14).toString()); // Default 14 days

        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setTotalPrice(quote.getTotalPrice());
        invoice.setGst(quote.getGst());
        if (quote.getDiscountType() != null) {
            try {
                invoice.setDiscountType(Invoice.DiscountType.valueOf(quote.getDiscountType()));
            } catch (IllegalArgumentException e) {
                // handle case where enum name might not match exactly or is legacy string
                logger.warn("Invalid discount type in quote: {}", quote.getDiscountType());
            }
        }
        invoice.setDiscountValue(quote.getDiscountValue());
        invoice.setNotes(quote.getNotes());

        List<InvoiceItem> invoiceItems = new ArrayList<>();
        if (quote.getQuoteItems() != null) {
            for (QuoteItem quoteItem : quote.getQuoteItems()) {
                InvoiceItem invoiceItem = new InvoiceItem();
                invoiceItem.setInvoice(invoice);
                invoiceItem.setServiceItem(quoteItem.getServiceItem());
                invoiceItem.setProductItem(quoteItem.getProductItem());
                invoiceItem.setItemDescription(quoteItem.getItemDescription());
                invoiceItem.setQuantity(quoteItem.getQuantity());
                invoiceItem.setItemOrder(quoteItem.getItemOrder());
                invoiceItem.setPrice(quoteItem.getPrice());
                invoiceItem.setTotal(quoteItem.getTotal());
                invoiceItem.setGst(quoteItem.getGst());
                invoiceItem.setCode(quoteItem.getCode());
                invoiceItems.add(invoiceItem);
            }
        }
        invoice.setInvoiceItems(invoiceItems);

        invoice = invoiceRepository.save(invoice);
        return invoiceMapper.toDto(invoice, false);
    }

    private void setClient(InvoiceDto invoiceDto, Long companyId, Invoice invoice) {
        if (invoiceDto.getClientId() != null) {
            Client client = clientRepository.findById(invoiceDto.getClientId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Client not found with id: " + invoiceDto.getClientId()));
            invoice.setClient(client);
        } else {
            // Try to find existing client by email or phone
            Optional<Client> existingClient = Optional.empty();

            if (StringUtils.isNotBlank(invoiceDto.getClientEmail())) {
                existingClient = clientRepository.findByEmailAndCompanyId(invoiceDto.getClientEmail(), companyId);
            }

            if (!existingClient.isPresent() && StringUtils.isNotBlank(invoiceDto.getClientPhone())) {
                existingClient = clientRepository.findByPhoneAndCompanyId(invoiceDto.getClientPhone(), companyId);
            }

            if (existingClient.isPresent()) {
                Client client = existingClient.get();
                // Optionally update client details here if needed
                invoice.setClient(client);
            } else {
                ClientDto clientDto = new ClientDto();
                clientDto.setEmail(invoiceDto.getClientEmail());
                clientDto.setPhone(invoiceDto.getClientPhone());
                clientDto.setCompanyId(invoiceDto.getCompanyId());
                clientDto.setFirstName(invoiceDto.getClientFirstname());
                clientDto.setLastName(invoiceDto.getClientLastname());
                clientDto.setEntityName(invoiceDto.getClientEntityName());

                logger.debug("Creating new client for invoice: {}", clientDto);
                clientDto = clientService.saveClient(clientDto, companyId);

                invoice.setClient(clientMapper.toEntity(clientDto));
            }
        }
    }

    private String generateInvoiceSequence(Long companyId) {
        // Fetch the SequenceConfig with pessimistic lock to prevent race conditions
        Optional<SequenceConfig> sequenceConfigOptional = sequenceConfigRepository
                .findByEntityTypeAndCompanyIdForUpdate(SequenceConfig.EntityType.INVOICE, companyId);

        if (!sequenceConfigOptional.isPresent()) {
            throw new IllegalStateException("Sequence configuration not found for INVOICE and company ID: "
                    + companyId + " please configure the sequence configuration for your Invoice");
        }

        SequenceConfig sequenceConfig = sequenceConfigOptional.get();
        Integer currentNumber = sequenceConfig.getCurrentNumber();

        int newInvoiceSequence = currentNumber + 1;

        // Generate the new invoice number with date placeholders and padding
        String newInvoiceNumber = io.quickledger.utils.SequenceNumberFormatter.format(
                sequenceConfig.getPrefix(),
                newInvoiceSequence,
                sequenceConfig.getPostfix(),
                sequenceConfig.getNumberPadding(),
                java.time.LocalDate.now());

        // Update the current number in the database
        sequenceConfig.setCurrentNumber(newInvoiceSequence);
        sequenceConfigRepository.save(sequenceConfig);

        return newInvoiceNumber;
    }

    public Page<InvoiceDto> getAllInvoices(Long companyId, boolean lazy, Pageable pageable, User user) {
        userCompanyService.checkUserBelongs(user, companyId);
        Page<Invoice> invoices = invoiceRepository.findAllByCompanyIdOrderByCreatedDateDesc(companyId, pageable);
        return invoices.map(invoice -> invoiceMapper.toDto(invoice, lazy));
    }

    public InvoiceDto getInvoiceById(Long companyId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));

        if (!invoice.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Invoice does not belong to company");
        }

        InvoiceDto invoiceDto = invoiceMapper.toDto(invoice, false);
        invoiceDto.setPaymentLink(generatePaymentLink(invoice));
        return invoiceDto;
    }

    public void deleteInvoice(Long companyId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));

        if (!invoice.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Invoice does not belong to company");
        }

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new IllegalStateException("Paid invoices cannot be deleted");
        }

        invoiceRepository.delete(invoice);
    }

    @Transactional(readOnly = true)
    public byte[] downloadInvoicePdf(Long companyId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));

        if (!invoice.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Invoice does not belong to company: " + companyId);
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("invoice", invoice);
        variables.put("company", invoice.getCompany());

        // Process Company Template Config
        Company company = invoice.getCompany();
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

        // Add company logo if available
        if (company.getImage() != null && company.getImage().length > 0) {
            String logoBase64 = Base64.getEncoder().encodeToString(company.getImage());
            variables.put("companyLogoBase64", logoBase64);

            // Use content type if available, otherwise default to image/png
            String contentType = company.getLogoContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/png";
            }
            variables.put("companyLogoContentType", contentType);
        }

        // Add banking details if available for payment instructions
        if (company.getBankBsb() != null && !company.getBankBsb().isEmpty()) {
            variables.put("companyBankBsb", company.getBankBsb());
        }
        if (company.getBankAccount() != null && !company.getBankAccount().isEmpty()) {
            variables.put("companyBankAccount", company.getBankAccount());
        }

        // Add Stripe Payment Link if connected
        String paymentLink = generatePaymentLink(invoice);
        if (paymentLink != null) {
            variables.put("paymentLink", paymentLink);
        } else {
            logger.warn("Payment link missing for invoice PDF {}. Company Stripe Connected: {}, Charges Enabled: {}",
                    invoiceId, company.getStripeConnectedAccountId() != null, company.isStripeChargesEnabled());
        }

        return pdfService.generatePdfFromHtml("invoice_pdf", variables);
    }

    @Transactional
    public InvoiceDto sendInvoice(Long companyId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));

        if (!invoice.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Invoice does not belong to company: " + companyId);
        }

        if (invoice.getClient() == null || StringUtils.isBlank(invoice.getClient().getEmail())) {
            throw new IllegalStateException("Client email not found for invoice: " + invoiceId);
        }

        // Generate PDF
        byte[] pdfBytes = downloadInvoicePdf(companyId, invoiceId);

        String clientName = invoice.getClient().getContactName();
        if (StringUtils.isBlank(clientName)) {
            clientName = invoice.getClient().getContactName() + " " + invoice.getClient().getContactSurname();
        }

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<html><body style='font-family: Arial, sans-serif;'>");

        // Header
        emailBody.append("<h2 style='color:#333;'>").append(invoice.getCompany().getName()).append("</h2>");

        emailBody.append("<p>Dear ").append(clientName).append(",</p>");
        emailBody.append("<p>Please find attached your invoice #").append(invoice.getInvoiceNumber()).append(".</p>");
        emailBody.append("<p><strong>Total Amount:</strong> $").append(invoice.getTotalPrice()).append("<br/>");
        emailBody.append("<strong>Due Date:</strong> ").append(invoice.getDueDate()).append("</p>");

        // Generate Stripe Payment Link if company is connected
        String paymentLink = generatePaymentLink(invoice);
        if (paymentLink != null) {
            emailBody.append("<p>You can pay securely online by clicking the button below:</p>");
            emailBody.append("<a href=\"").append(paymentLink)
                    .append("\" style=\"background-color:#007bff;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;display:inline-block;\">Pay Now</a>");
            emailBody.append("<br/><br/>");
        }

        // Generate Secure Online Invoice Link
        long expirationTimeMillis = 30L * 24 * 60 * 60 * 1000;
        String token = tempTokenService.getOrCreateInvoiceToken(invoice.getClient().getId(), companyId, invoiceId,
                expirationTimeMillis);

        String onlineLink = applicationFrontendUrl + "/public/invoices/view?token=" + token;

        emailBody.append("<p>You can also view your invoice online here:</p>");
        emailBody.append("<a href=\"").append(onlineLink).append("\">View Invoice Online</a>");
        emailBody.append("<br/><br/>");

        // Footer with Contact Info
        emailBody.append("<hr style='border:none;border-top:1px solid #eee;margin:20px 0;'/>");
        emailBody.append("<p style='font-size:0.9em;color:#666;'>Please do not reply to this email.</p>");

        String companyEmail = invoice.getCompany().getEmail();
        String companyPhone = invoice.getCompany().getPhone();

        if (StringUtils.isNotBlank(companyEmail) || StringUtils.isNotBlank(companyPhone)) {
            emailBody.append("<p style='font-size:0.9em;color:#666;'>If you have any questions, please contact ")
                    .append(invoice.getCompany().getName());

            if (StringUtils.isNotBlank(companyEmail)) {
                emailBody.append(" at <a href='mailto:").append(companyEmail).append("'>").append(companyEmail)
                        .append("</a>");
            }

            if (StringUtils.isNotBlank(companyPhone)) {
                emailBody.append(StringUtils.isNotBlank(companyEmail) ? " or " : " on ");
                emailBody.append(companyPhone);
            }
            emailBody.append(".</p>");
        }

        emailBody.append("</body></html>");

        String pdfFileName = "invoice-" + invoice.getInvoiceNumber() + ".pdf";

        // Subject: Invoice #... from Company
        String subject = "Invoice #" + invoice.getInvoiceNumber() + " from " + invoice.getCompany().getName();

        emailService.sendEmailWithAttachment(
                invoice.getClient().getEmail(),
                subject,
                emailBody.toString(),
                pdfBytes,
                pdfFileName,
                invoice.getCompany().getName(), // From Name
                invoice.getCompany().getEmail(), // Reply To
                true);

        // Update invoice status to SENT if it was DRAFT
        if (invoice.getStatus() == Invoice.InvoiceStatus.DRAFT) {
            invoice.setStatus(Invoice.InvoiceStatus.SENT);
            invoiceRepository.save(invoice);
        }

        InvoiceDto invoiceDto = invoiceMapper.toDto(invoice, false);
        invoiceDto.setPaymentLink(generatePaymentLink(invoice));
        return invoiceDto;
    }

    public String getPublicInvoiceLink(Long companyId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));

        if (!invoice.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Invoice does not belong to company: " + companyId);
        }

        if (invoice.getClient() == null) {
            throw new IllegalStateException("Invoice does not have a client assigned.");
        }

        // 30 days expiration
        long expirationTimeMillis = 30L * 24 * 60 * 60 * 1000;
        String token = tempTokenService.getOrCreateInvoiceToken(invoice.getClient().getId(), companyId, invoiceId,
                expirationTimeMillis);

        return applicationFrontendUrl + "/public/invoices/view?token=" + token;
    }

    private void calculateInvoiceTotals(Invoice invoice) {
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;

        if (invoice.getInvoiceItems() != null) {
            for (InvoiceItem item : invoice.getInvoiceItems()) {
                BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                BigDecimal gstRateVal = item.getGst() != null ? item.getGst() : BigDecimal.ZERO;

                BigDecimal lineTotalExGst = qty.multiply(price);
                BigDecimal lineTotalIncGst = lineTotalExGst;
                BigDecimal lineGst = BigDecimal.ZERO;

                if (gstRateVal.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal multiplier = BigDecimal.ONE
                            .add(gstRateVal.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP));
                    lineTotalIncGst = lineTotalExGst.multiply(multiplier).setScale(2, java.math.RoundingMode.HALF_UP);
                    lineGst = lineTotalIncGst.subtract(lineTotalExGst);
                }

                item.setTotal(lineTotalIncGst);
                subTotal = subTotal.add(lineTotalIncGst);
                totalGst = totalGst.add(lineGst);
            }
        }

        // Apply Discount
        BigDecimal finalTotal = subTotal;
        BigDecimal discountVal = invoice.getDiscountValue() != null ? invoice.getDiscountValue() : BigDecimal.ZERO;

        if (invoice.getDiscountType() == Invoice.DiscountType.DOLLAR) {
            finalTotal = finalTotal.subtract(discountVal);
        } else if (invoice.getDiscountType() == Invoice.DiscountType.PERCENT) {
            // percentage off the total
            BigDecimal discountAmount = finalTotal.multiply(discountVal)
                    .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
            finalTotal = finalTotal.subtract(discountAmount);
        }

        invoice.setTotalPrice(finalTotal);
        invoice.setGst(totalGst);
    }

    private String generatePaymentLink(Invoice invoice) {
        Company company = invoice.getCompany();
        if (company.getStripeConnectedAccountId() != null
                && company.isStripeChargesEnabled()) {
            try {
                long amountCents = invoice.getTotalPrice().multiply(java.math.BigDecimal.valueOf(100)).longValue();
                String successUrl = applicationFrontendUrl + "/invoices/" + invoice.getId() + "?payment=success";
                String cancelUrl = applicationFrontendUrl + "/invoices/" + invoice.getId() + "?payment=cancel";

                return stripeService.createInvoicePaymentSession(
                        invoice.getId(),
                        amountCents,
                        "aud",
                        "Invoice #" + invoice.getInvoiceNumber(),
                        company.getId(),
                        invoice.getClient().getEmail(),
                        successUrl,
                        cancelUrl);
            } catch (Exception e) {
                logger.error("Failed to generate payment link for invoice " + invoice.getId(), e);
            }
        } else {
            logger.debug("Stripe not fully enabled for company {}. AccountId present: {}, Charges Enabled: {}",
                    company.getId(),
                    company.getStripeConnectedAccountId() != null,
                    company.isStripeChargesEnabled());
        }
        return null;
    }
}

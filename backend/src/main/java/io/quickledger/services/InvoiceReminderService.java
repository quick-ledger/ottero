package io.quickledger.services;

import io.quickledger.entities.Company;
import io.quickledger.entities.invoice.Invoice;
import io.quickledger.entities.invoice.InvoiceReminder;
import io.quickledger.entities.invoice.InvoiceReminder.ReminderType;
import io.quickledger.repositories.InvoiceReminderRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InvoiceReminderService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceReminderService.class);

    private final InvoiceReminderRepository invoiceReminderRepository;
    private final InvoiceService invoiceService;
    private final EmailService emailService;
    private final TempTokenService tempTokenService;
    private final StripeService stripeService;

    @Value("${application.frontend.url}")
    private String applicationFrontendUrl;

    public InvoiceReminderService(InvoiceReminderRepository invoiceReminderRepository,
                                   InvoiceService invoiceService,
                                   EmailService emailService,
                                   TempTokenService tempTokenService,
                                   StripeService stripeService) {
        this.invoiceReminderRepository = invoiceReminderRepository;
        this.invoiceService = invoiceService;
        this.emailService = emailService;
        this.tempTokenService = tempTokenService;
        this.stripeService = stripeService;
    }

    @Transactional
    public boolean sendReminderIfNotAlreadySent(Invoice invoice, ReminderType reminderType) {
        if (invoiceReminderRepository.existsByInvoiceIdAndReminderTypeAndSuccessTrue(
                invoice.getId(), reminderType)) {
            logger.debug("Reminder {} already sent for invoice {}", reminderType, invoice.getId());
            return false;
        }

        if (invoice.getClient() == null || StringUtils.isBlank(invoice.getClient().getEmail())) {
            logger.warn("No client email for invoice {}", invoice.getId());
            return false;
        }

        return sendReminder(invoice, reminderType);
    }

    private boolean sendReminder(Invoice invoice, ReminderType reminderType) {
        InvoiceReminder reminder = new InvoiceReminder();
        reminder.setInvoice(invoice);
        reminder.setReminderType(reminderType);
        reminder.setSentAt(LocalDateTime.now());
        reminder.setRecipientEmail(invoice.getClient().getEmail());

        try {
            String subject = buildSubject(invoice, reminderType);
            String body = buildEmailBody(invoice, reminderType);

            byte[] pdfBytes = invoiceService.downloadInvoicePdf(
                    invoice.getCompany().getId(), invoice.getId());
            String pdfFileName = "invoice-" + invoice.getInvoiceNumber() + ".pdf";

            emailService.sendEmailWithAttachment(
                    invoice.getClient().getEmail(),
                    subject,
                    body,
                    pdfBytes,
                    pdfFileName,
                    invoice.getCompany().getName(),
                    invoice.getCompany().getEmail(),
                    true);

            reminder.setSuccess(true);
            invoiceReminderRepository.save(reminder);

            logger.info("Sent {} reminder for invoice #{} to {}",
                    reminderType, invoice.getInvoiceNumber(), invoice.getClient().getEmail());
            return true;

        } catch (Exception e) {
            logger.error("Failed to send {} reminder for invoice {}: {}",
                    reminderType, invoice.getId(), e.getMessage());

            reminder.setSuccess(false);
            reminder.setErrorMessage(StringUtils.abbreviate(e.getMessage(), 2000));
            invoiceReminderRepository.save(reminder);
            return false;
        }
    }

    private String buildSubject(Invoice invoice, ReminderType reminderType) {
        String invoiceNumber = invoice.getInvoiceNumber();
        String companyName = invoice.getCompany().getName();

        return switch (reminderType) {
            case UPCOMING_3_DAYS -> "Payment Reminder: Invoice #" + invoiceNumber + " due in 3 days";
            case OVERDUE_7_DAYS -> "Payment Overdue: Invoice #" + invoiceNumber + " from " + companyName;
        };
    }

    private String buildEmailBody(Invoice invoice, ReminderType reminderType) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<html><body style='font-family: Arial, sans-serif;'>");

        // Header
        emailBody.append("<h2 style='color:#333;'>").append(invoice.getCompany().getName()).append("</h2>");

        // Client name
        String clientName = invoice.getClient().getContactName();
        if (StringUtils.isNotBlank(invoice.getClient().getContactSurname())) {
            clientName = clientName + " " + invoice.getClient().getContactSurname();
        }
        emailBody.append("<p>Dear ").append(clientName).append(",</p>");

        // Reminder-specific message
        String reminderMessage = switch (reminderType) {
            case UPCOMING_3_DAYS -> "This is a friendly reminder that your invoice is due in 3 days.";
            case OVERDUE_7_DAYS -> "Your payment is now 7 days overdue. Please arrange payment at your earliest convenience.";
        };
        emailBody.append("<p>").append(reminderMessage).append("</p>");

        // Invoice details
        emailBody.append("<p><strong>Invoice Number:</strong> #").append(invoice.getInvoiceNumber()).append("<br/>");
        emailBody.append("<strong>Total Amount:</strong> $").append(invoice.getTotalPrice()).append("<br/>");
        emailBody.append("<strong>Due Date:</strong> ").append(invoice.getDueDate()).append("</p>");

        // Stripe Payment Link
        String paymentLink = generatePaymentLink(invoice);
        if (paymentLink != null) {
            emailBody.append("<p>You can pay securely online by clicking the button below:</p>");
            emailBody.append("<a href=\"").append(paymentLink)
                    .append("\" style=\"background-color:#007bff;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;display:inline-block;\">Pay Now</a>");
            emailBody.append("<br/><br/>");
        }

        // View Invoice Online Link
        long expirationTimeMillis = 30L * 24 * 60 * 60 * 1000; // 30 days
        String token = tempTokenService.getOrCreateInvoiceToken(
                invoice.getClient().getId(),
                invoice.getCompany().getId(),
                invoice.getId(),
                expirationTimeMillis);
        String onlineLink = applicationFrontendUrl + "/public/invoices/view?token=" + token;

        emailBody.append("<p>You can also view your invoice online here:</p>");
        emailBody.append("<a href=\"").append(onlineLink).append("\">View Invoice Online</a>");
        emailBody.append("<br/><br/>");

        // Footer
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
        return emailBody.toString();
    }

    private String generatePaymentLink(Invoice invoice) {
        Company company = invoice.getCompany();
        if (company.getStripeConnectedAccountId() != null && company.isStripeChargesEnabled()) {
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
        }
        return null;
    }
}

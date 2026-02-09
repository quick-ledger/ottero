package io.quickledger.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String appBaseUrl;

    public EmailService(JavaMailSender mailSender, @Value("${application.frontend.url}") String appBaseUrl) {
        this.mailSender = mailSender;
        this.appBaseUrl = appBaseUrl;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("info@ottero.com.au");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Error sending email to {}: {}", to, e.getMessage());
            throw e;
        }
    }

    /**
     * Send email with PDF attachment
     * 
     * @param to          recipient email address
     * @param subject     email subject
     * @param body        email body text
     * @param pdfBytes    PDF file content as byte array
     * @param pdfFileName name of the PDF file (e.g., "quote-123.pdf")
     */
    public void sendEmailWithAttachment(String to, String subject, String body, byte[] pdfBytes, String pdfFileName) {
        sendEmailWithAttachment(to, subject, body, pdfBytes, pdfFileName, null, null, false);
    }

    public void sendEmailWithAttachment(String to, String subject, String body, byte[] pdfBytes, String pdfFileName,
            String fromName, String replyTo, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromName != null && !fromName.isEmpty()) {
                helper.setFrom("info@ottero.com.au", fromName);
            } else {
                helper.setFrom("info@ottero.com.au");
            }

            if (replyTo != null && !replyTo.isEmpty()) {
                helper.setReplyTo(replyTo);
            }

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);

            // Add PDF attachment
            helper.addAttachment(pdfFileName, new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            logger.info("Email with attachment sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Error sending email with attachment to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

    /**
     * Send trial started notification email
     */
    public void sendTrialStartedEmail(String to, String userName, String planName) {
        String subject = "Welcome to " + planName + " Plan - Your 30-Day Free Trial Has Started!";
        String body = "Dear " + userName + ",\n\n" +
                "Welcome to Ottero! Your 30-day free trial of the " + planName + " plan has started.\n\n" +
                "You now have full access to all " + planName + " features for the next 30 days.\n\n" +
                "What happens next?\n" +
                "- Enjoy full access to all features during your trial\n" +
                "- We'll send you a reminder 3 days before your trial ends\n" +
                "- After 30 days, your subscription will automatically continue at the regular price\n" +
                "- You can cancel anytime from your account settings\n\n" +
                "Get started now: " + appBaseUrl + "\n\n" +
                "Best regards,\n" +
                "The Ottero Team";

        sendEmail(to, subject, body);
    }

    /**
     * Send trial ending soon notification email (3 days before trial ends)
     */
    public void sendTrialEndingSoonEmail(String to, String userName, String planName) {
        String subject = "Your " + planName + " Trial Ends in 3 Days";
        String body = "Dear " + userName + ",\n\n" +
                "This is a friendly reminder that your 30-day free trial of the " + planName
                + " plan will end in 3 days.\n\n" +
                "After your trial ends, your subscription will automatically continue and you'll be charged the regular price.\n\n"
                +
                "What you can do:\n" +
                "- Continue enjoying all features - no action needed\n" +
                "- Cancel anytime from your account settings if you don't wish to continue\n" +
                "- Update your payment method if needed\n\n" +
                "Manage your subscription: " + appBaseUrl + "/settings/billing\n\n" +
                "Best regards,\n" +
                "The Ottero Team";

        sendEmail(to, subject, body);
    }

    /**
     * Send subscription activated email (after trial ends and first payment
     * succeeds)
     */
    public void sendSubscriptionActivatedEmail(String to, String userName, String planName) {
        String subject = "Thank You for Subscribing to " + planName + " Plan!";
        String body = "Dear " + userName + ",\n\n" +
                "Thank you for subscribing to the " + planName + " plan!\n\n" +
                "Your payment was processed successfully, and your subscription is now active.\n\n" +
                "You'll continue to have full access to all " + planName + " features.\n\n" +
                "Manage your subscription: " + appBaseUrl + "/settings/billing\n\n" +
                "Best regards,\n" +
                "The Ottero Team";

        sendEmail(to, subject, body);
    }

    /**
     * Send payment failed notification email
     */
    public void sendPaymentFailedEmail(String to, String userName) {
        String subject = "Payment Failed - Action Required";
        String body = "Dear " + userName + ",\n\n" +
                "We were unable to process your payment for your Ottero subscription.\n\n" +
                "To avoid any interruption to your service, please update your payment method as soon as possible.\n\n"
                +
                "Update payment method: " + appBaseUrl + "/settings/billing\n\n" +
                "If you have any questions or need assistance, please don't hesitate to contact us.\n\n" +
                "Best regards,\n" +
                "The Ottero Team";

        sendEmail(to, subject, body);
    }

    /**
     * Send subscription canceled notification email
     */
    public void sendSubscriptionCanceledEmail(String to, String userName) {
        String subject = "Subscription Canceled - We're Sorry to See You Go";
        String body = "Dear " + userName + ",\n\n" +
                "Your subscription has been canceled.\n\n" +
                "What happens next:\n" +
                "- You've been moved to the Free plan\n" +
                "- All your data has been preserved and remains accessible\n" +
                "- You can create up to 5 quotes/invoices per month on the Free plan\n" +
                "- You can upgrade again anytime to unlock unlimited access\n\n" +
                "We'd love to have you back! If you have any feedback about why you canceled, " +
                "please reply to this email - we read every response.\n\n" +
                "Upgrade anytime: " + appBaseUrl + "/settings/billing\n\n" +
                "Best regards,\n" +
                "The Ottero Team";

        sendEmail(to, subject, body);
    }
}

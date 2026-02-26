package io.quickledger.services;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import io.quickledger.entities.User;
import io.quickledger.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.quickledger.repositories.CompanyRepository;
import io.quickledger.repositories.InvoiceRepository;
import io.quickledger.entities.invoice.Invoice;

@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Value("${application.frontend.url:https://dashboard.ottero.com.au}")
    private String baseUrl;

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${stripe.client.id}")
    private String stripeClientId;

    @Value("${stripe.platform.fee.cents:100}")
    private Long platformFeeCents;

    private final CompanyRepository companyRepository;
    private final InvoiceRepository invoiceRepository;

    public StripeService(UserRepository userRepository, EmailService emailService,
            CompanyRepository companyRepository, InvoiceRepository invoiceRepository) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.companyRepository = companyRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        // Also set client ID on Stripe object if needed, but usually passed in OAuth
        // params
        // Stripe.clientId = stripeClientId; // Not a standard field on Stripe global
        // object
    }

    public String createStripeConnectOAuthUrl(String state) {
        // Construct the URL manually or use a helper if available, but manual is often
        // easier for this specific one
        // https://connect.stripe.com/oauth/authorize?response_type=code&client_id=ca_...&scope=read_write&state=...
        return String.format(
                "https://connect.stripe.com/oauth/authorize?response_type=code&client_id=%s&scope=read_write&state=%s",
                stripeClientId, state);
    }

    public String connectCompanyAccount(String code, Long companyId) throws StripeException {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("client_id", stripeClientId); // REQUIRED: Must match the client_id used in OAuth URL

        logger.info("Stripe OAuth token exchange - client_id: {}, code: {}", stripeClientId,
                code.substring(0, 10) + "...");

        // OAuth uses client credentials for authentication
        com.stripe.model.oauth.TokenResponse response = com.stripe.net.OAuth.token(params, null);

        String connectedAccountId = response.getStripeUserId();

        companyRepository.findById(companyId).ifPresent(company -> {
            company.setStripeConnectedAccountId(connectedAccountId);
            company.setStripeChargesEnabled(true); // Assuming true upon connection, or we can check
                                                   // response.getLivemode() or fetch account details
            companyRepository.save(company);
        });

        return connectedAccountId;
    }

    /**
     * Creates a checkout session for a customer to pay an invoice.
     * Funds are transferred to the Company's connected account.
     * Fees are added on top so the payer pays them (surcharging).
     */
    public String createInvoicePaymentSession(Long invoiceId, Long amountCents, String currency, String description,
            Long companyId, String customerEmail, String successUrl, String cancelUrl) throws StripeException {
        io.quickledger.entities.Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (company.getStripeConnectedAccountId() == null) {
            throw new IllegalStateException("Company is not connected to Stripe");
        }

        // Calculate Application Fee (Platform Fee) - Configurable via properties
        long applicationFeeAmount = platformFeeCents;

        // Calculate Gross Amount (Surcharging)
        // Gross = (Net + FixedFee) / (1 - %Fee)
        // Stripe fees in AU: 1.75% + 30c
        double stripePct = 0.0175;
        long stripeFixed = 30L;

        // We want the company to receive 'amountCents'.
        // The total charge should cover 'amountCents' + stripe fees + application fee?
        // Plan says: Gross = (Net + FixedFee) / (1 - %Fee)
        // This usually covers the Stripe fee. We also need to cover the application fee
        // if we want the user to get the full amount.

        // Let's assume 'amountCents' is what the user wants in their pocket.
        // We add Application Fee to the Net needed.
        long netNeeded = amountCents + applicationFeeAmount;

        // Calculate Gross to cover Stripe Fees
        long grossAmount = Math.round((netNeeded + stripeFixed) / (1.0 - stripePct));

        // Calculate Surcharge
        long surchargeAmount = grossAmount - amountCents;

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .putMetadata("invoiceId", String.valueOf(invoiceId))
                .putMetadata("type", "invoice_payment")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(amountCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(description)
                                                                .build())
                                                .build())
                                .build())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(surchargeAmount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Online Payment Surcharge & Fees")
                                                                .build())
                                                .build())
                                .build())
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .setTransferData(
                                        SessionCreateParams.PaymentIntentData.TransferData.builder()
                                                .setDestination(company.getStripeConnectedAccountId())
                                                .build())
                                .setApplicationFeeAmount(applicationFeeAmount)
                                .build());

        if (customerEmail != null && !customerEmail.isEmpty()) {
            paramsBuilder.setCustomerEmail(customerEmail);
        }

        Session session = Session.create(paramsBuilder.build());
        return session.getUrl();
    }

    public String createCheckoutSession(String userExternalId, String priceId, String planName) throws StripeException {
        // Fetch user to get details/email if needed
        User user = userRepository.findByExternalId(userExternalId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(baseUrl + "/settings/profile?success=true&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseUrl + "/settings/profile?canceled=true")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPrice(priceId)
                                .build())
                .putMetadata("userExternalId", userExternalId)
                .putMetadata("planName", planName);

        // If user already has a Stripe customer ID, use it to avoid duplicate customers
        if (user.getStripeCustomerId() != null && !user.getStripeCustomerId().isEmpty()) {
            paramsBuilder.setCustomer(user.getStripeCustomerId());
        } else if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            // Otherwise pre-fill email for new customer creation
            paramsBuilder.setCustomerEmail(user.getEmail());
        }

        // Enable promotion codes
        paramsBuilder.setAllowPromotionCodes(true);

        // Set explicit locale to prevent Stripe's dynamic locale loading error
        paramsBuilder.setLocale(SessionCreateParams.Locale.EN);

        // Add 30-day trial for Basic plan
        if ("Basic".equalsIgnoreCase(planName)) {
            paramsBuilder.setSubscriptionData(
                    SessionCreateParams.SubscriptionData.builder()
                            .setTrialPeriodDays(30L)
                            .build());
        }

        try {
            Session session = Session.create(paramsBuilder.build());
            return session.getUrl();
        } catch (com.stripe.exception.InvalidRequestException e) {
            if (e.getMessage() != null && e.getMessage().contains("No such customer")) {
                logger.warn("Stripe customer {} not found during checkout. Clearing and retrying.",
                        user.getStripeCustomerId());
                user.setStripeCustomerId(null);
                user.setStripeSubscriptionId(null);
                user.setSubscriptionPlan("Free");
                user.setSubscriptionStatus("free");
                userRepository.save(user);

                // Retry by recursively calling the method (now with null ID)
                return createCheckoutSession(userExternalId, priceId, planName);
            }
            throw e;
        }
    }

    public void handleWebhook(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            logger.error("Invalid Stripe Signature", e);
            throw new IllegalArgumentException("Invalid signature");
        } catch (Exception e) {
            logger.error("Webhook error", e);
            throw new RuntimeException("Webhook processing failed");
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                // Payment is successful and the subscription is created.
                handleCheckoutSessionCompleted(event);
                break;
            case "invoice.payment_succeeded":
                // Continue to provision the subscription as payments continue to be made.
                handleInvoicePaymentSucceeded(event);
                break;
            case "invoice.payment_failed":
                // The payment failed or the customer does not have a valid payment method.
                handleInvoicePaymentFailed(event);
                break;
            case "customer.subscription.updated":
                // Subscription was updated (e.g., plan changed, trial ended)
                handleSubscriptionUpdated(event);
                break;
            case "customer.subscription.deleted":
                // Subscription was canceled
                handleSubscriptionDeleted(event);
                break;
            default:
                logger.debug("Unhandled event type: {}", event.getType());
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = null;
        if (event.getDataObjectDeserializer().getObject().isPresent()) {
            session = (Session) event.getDataObjectDeserializer().getObject().get();
        } else {
            try {
                session = (Session) event.getDataObjectDeserializer().deserializeUnsafe();
                logger.warn("Using unsafe deserialization for checkout session event {}", event.getId());
            } catch (Exception e) {
                logger.error("Failed to deserialize session unsafe for event {}", event.getId(), e);
            }
        }

        if (session != null) {
            String userExternalId = session.getMetadata().get("userExternalId");
            String planName = session.getMetadata().get("planName");

            final Session finalSession = session; // Capture for lambda usage

            // Check if this is a one-off invoice payment
            if ("invoice_payment".equals(session.getMetadata().get("type"))) {
                handleInvoicePaymentCompleted(session);
                return;
            }

            if (userExternalId != null) {
                userRepository.findByExternalId(userExternalId).ifPresent(user -> {
                    user.setStripeCustomerId(finalSession.getCustomer());
                    user.setStripeSubscriptionId(finalSession.getSubscription());
                    user.setSubscriptionPlan(planName);
                    user.setSubscriptionStatus("trialing"); // Start with trialing status
                    user.setCancelAtPeriodEnd(false); // Reset cancel flag

                    // Set trial end date to 30 days from now
                    user.setTrialEndDate(java.time.LocalDateTime.now().plusDays(30));
                    user.setTrialReminderSent(false); // Reset reminder flag

                    userRepository.save(user);
                    logger.info("Updated subscription for user {}: Plan={}, Status=trialing, TrialEnds={}",
                            userExternalId, planName, user.getTrialEndDate());

                    // Send trial started email
                    try {
                        String userName = user.getName() != null ? user.getName() : "Valued Customer";
                        emailService.sendTrialStartedEmail(user.getEmail(), userName, planName);
                    } catch (Exception e) {
                        logger.error("Failed to send trial started email to user {}", userExternalId, e);
                    }
                });
            }
        } else {
            logger.warn("Could not deserialize session object for event {}", event.getId());
        }
    }

    private void handleInvoicePaymentCompleted(Session session) {
        String invoiceIdStr = session.getMetadata().get("invoiceId");
        if (invoiceIdStr != null) {
            try {
                Long invoiceId = Long.parseLong(invoiceIdStr);
                invoiceRepository.findById(invoiceId).ifPresent(invoice -> {
                    invoice.setStatus(Invoice.InvoiceStatus.PAID);
                    invoiceRepository.save(invoice);
                    logger.info("Invoice {} marked as PAID via Stripe webhook", invoiceId);

                    // Optional: Send receipt email logic here
                });
            } catch (NumberFormatException e) {
                logger.error("Invalid invoiceId in metadata: {}", invoiceIdStr);
            }
        }
    }

    private void handleInvoicePaymentSucceeded(Event event) {
        try {
            com.stripe.model.Invoice invoice = null;
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                invoice = (com.stripe.model.Invoice) event.getDataObjectDeserializer().getObject().get();
            } else {
                try {
                    invoice = (com.stripe.model.Invoice) event.getDataObjectDeserializer().deserializeUnsafe();
                    logger.warn("Using unsafe deserialization for invoice payment succeeded event {}", event.getId());
                } catch (Exception e) {
                    logger.error("Failed to deserialize invoice unsafe for event {}", event.getId(), e);
                }
            }

            if (invoice != null && invoice.getCustomer() != null) {
                String customerId = invoice.getCustomer();

                // ✅ Optimized: Use indexed query instead of findAll()
                userRepository.findByStripeCustomerId(customerId).ifPresent(user -> {
                    boolean wasTrialing = "trialing".equalsIgnoreCase(user.getSubscriptionStatus());
                    boolean wasPastDue = "past_due".equalsIgnoreCase(user.getSubscriptionStatus());

                    user.setSubscriptionStatus("active");
                    userRepository.save(user);
                    logger.info("Payment succeeded for user {}: Status=active (was: {})",
                            user.getExternalId(), wasTrialing ? "trialing" : wasPastDue ? "past_due" : "other");

                    // Send subscription activated email if transitioning from trial to active
                    if (wasTrialing) {
                        try {
                            String userName = user.getName() != null ? user.getName() : "Valued Customer";
                            String planName = user.getSubscriptionPlan() != null ? user.getSubscriptionPlan()
                                    : "Premium";
                            emailService.sendSubscriptionActivatedEmail(user.getEmail(), userName, planName);
                        } catch (Exception e) {
                            logger.error("Failed to send subscription activated email to user {}",
                                    user.getExternalId(), e);
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Error handling invoice.payment_succeeded", e);
        }
    }

    private void handleInvoicePaymentFailed(Event event) {
        try {
            com.stripe.model.Invoice invoice = null;
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                invoice = (com.stripe.model.Invoice) event.getDataObjectDeserializer().getObject().get();
            } else {
                try {
                    invoice = (com.stripe.model.Invoice) event.getDataObjectDeserializer().deserializeUnsafe();
                    logger.warn("Using unsafe deserialization for invoice payment failed event {}", event.getId());
                } catch (Exception e) {
                    logger.error("Failed to deserialize invoice unsafe for event {}", event.getId(), e);
                }
            }

            if (invoice != null && invoice.getCustomer() != null) {
                String customerId = invoice.getCustomer();

                // ✅ Optimized: Use indexed query instead of findAll()
                userRepository.findByStripeCustomerId(customerId).ifPresent(user -> {
                    user.setSubscriptionStatus("past_due");
                    userRepository.save(user);
                    logger.warn("Payment failed for user {}: Status=past_due (Stripe will retry automatically)",
                            user.getExternalId());

                    // Send payment failed email notification
                    try {
                        String userName = user.getName() != null ? user.getName() : "Valued Customer";
                        emailService.sendPaymentFailedEmail(user.getEmail(), userName);
                    } catch (Exception e) {
                        logger.error("Failed to send payment failed email to user {}",
                                user.getExternalId(), e);
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Error handling invoice.payment_failed", e);
        }
    }

    private void handleSubscriptionUpdated(Event event) {
        try {
            com.stripe.model.Subscription subscription = null;
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                subscription = (com.stripe.model.Subscription) event.getDataObjectDeserializer().getObject().get();
            } else {
                try {
                    subscription = (com.stripe.model.Subscription) event.getDataObjectDeserializer()
                            .deserializeUnsafe();
                    logger.warn("Using unsafe deserialization for subscription updated event {}", event.getId());
                } catch (Exception e) {
                    logger.error("Failed to deserialize subscription unsafe for event {}", event.getId(), e);
                }
            }

            if (subscription != null && subscription.getCustomer() != null) {
                String customerId = subscription.getCustomer();
                String status = subscription.getStatus();

                // ✅ Optimized: Use indexed query instead of findAll()
                final com.stripe.model.Subscription finalSubscription = subscription;
                userRepository.findByStripeCustomerId(customerId).ifPresent(user -> {
                    user.setSubscriptionStatus(status);
                    user.setCancelAtPeriodEnd(finalSubscription.getCancelAtPeriodEnd());

                    // ✅ Handle unpaid status (after all Stripe retries exhausted)
                    if ("unpaid".equalsIgnoreCase(status)) {
                        user.setSubscriptionPlan("Free");
                        user.setStripeSubscriptionId(null);
                        logger.warn(
                                "Subscription unpaid for user {}: Reverted to Free plan (all payment retries failed)",
                                user.getExternalId());
                    }

                    userRepository.save(user);
                    logger.info("Subscription updated for user {}: Status={}", user.getExternalId(), status);
                });
            }
        } catch (Exception e) {
            logger.error("Error handling customer.subscription.updated", e);
        }
    }

    private void handleSubscriptionDeleted(Event event) {
        try {
            com.stripe.model.Subscription subscription = null;
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                subscription = (com.stripe.model.Subscription) event.getDataObjectDeserializer().getObject().get();
            } else {
                try {
                    subscription = (com.stripe.model.Subscription) event.getDataObjectDeserializer()
                            .deserializeUnsafe();
                    logger.warn("Using unsafe deserialization for subscription deleted event {}", event.getId());
                } catch (Exception e) {
                    logger.error("Failed to deserialize subscription unsafe for event {}", event.getId(), e);
                }
            }

            if (subscription != null && subscription.getCustomer() != null) {
                String customerId = subscription.getCustomer();

                // ✅ Optimized: Use indexed query instead of findAll()
                userRepository.findByStripeCustomerId(customerId).ifPresent(user -> {
                    user.setSubscriptionPlan("Free");
                    user.setSubscriptionStatus("canceled");
                    user.setStripeSubscriptionId(null);
                    userRepository.save(user);
                    logger.info("Subscription canceled for user {}: Reverted to Free plan",
                            user.getExternalId());

                    // ✅ Send cancellation confirmation email
                    try {
                        String userName = user.getName() != null ? user.getName() : "Valued Customer";
                        emailService.sendSubscriptionCanceledEmail(user.getEmail(), userName);
                        logger.info("Sent cancellation email to user {}", user.getExternalId());
                    } catch (Exception e) {
                        logger.error("Failed to send cancellation email to user {}",
                                user.getExternalId(), e);
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Error handling customer.subscription.deleted", e);
        }
    }

    public String createCustomerPortalSession(String userExternalId) throws StripeException {
        User user = userRepository.findByExternalId(userExternalId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStripeCustomerId() == null) {
            throw new IllegalArgumentException("User does not have a Stripe customer ID");
        }

        try {
            com.stripe.param.billingportal.SessionCreateParams params = com.stripe.param.billingportal.SessionCreateParams
                    .builder()
                    .setCustomer(user.getStripeCustomerId())
                    .setReturnUrl(baseUrl + "/settings/profile")
                    .build();

            com.stripe.model.billingportal.Session portalSession = com.stripe.model.billingportal.Session
                    .create(params);

            logger.info("Created customer portal session for user {}", userExternalId);
            return portalSession.getUrl();
        } catch (com.stripe.exception.InvalidRequestException e) {
            // Handle case where Stripe customer was deleted (e.g. in test mode or manually)
            if (e.getMessage() != null && e.getMessage().contains("No such customer")) {
                logger.warn("Stripe customer {} not found. Clearing from user record.", user.getStripeCustomerId());
                user.setStripeCustomerId(null);
                user.setStripeSubscriptionId(null);
                user.setSubscriptionPlan("Free");
                user.setSubscriptionStatus("free");
                userRepository.save(user);

                // Redirect user to pricing page to re-subscribe
                return baseUrl + "/settings/pricing";
            }
            throw e;
        }
    }

    /**
     * Apply referral discount (100% off for one month) to a subscription.
     * Requires a coupon named "referral_free_month" to be created in Stripe Dashboard:
     * - Duration: once
     * - Percent off: 100%
     */
    public void applyReferralDiscount(String stripeSubscriptionId) throws StripeException {
        logger.info("Applying referral discount to subscription: {}", stripeSubscriptionId);

        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCoupon("referral_free_month")
                .build();

        Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
        subscription.update(params);

        logger.info("Successfully applied referral discount to subscription: {}", stripeSubscriptionId);
    }
}

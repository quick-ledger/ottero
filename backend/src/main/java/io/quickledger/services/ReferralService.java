package io.quickledger.services;

import com.stripe.exception.StripeException;
import io.quickledger.dto.ReferralDto;
import io.quickledger.entities.Referral;
import io.quickledger.entities.ReferralStatus;
import io.quickledger.entities.User;
import io.quickledger.mappers.ReferralMapper;
import io.quickledger.repositories.ReferralRepository;
import io.quickledger.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReferralService {

    private static final Logger logger = LoggerFactory.getLogger(ReferralService.class);

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final StripeService stripeService;

    public ReferralService(ReferralRepository referralRepository, UserRepository userRepository,
                          EmailService emailService, StripeService stripeService) {
        this.referralRepository = referralRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.stripeService = stripeService;
    }

    @Transactional
    public ReferralDto createReferral(String userExternalId, String refereeEmail, String refereeName) {
        User referrer = userRepository.findByExternalId(userExternalId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if already referred by this user
        if (referralRepository.existsByReferrerAndRefereeEmail(referrer, refereeEmail.toLowerCase())) {
            throw new IllegalArgumentException("You have already referred this email address");
        }

        // Check if user is trying to refer themselves
        if (referrer.getEmail() != null && referrer.getEmail().equalsIgnoreCase(refereeEmail)) {
            throw new IllegalArgumentException("You cannot refer yourself");
        }

        Referral referral = new Referral();
        referral.setReferrer(referrer);
        referral.setRefereeEmail(refereeEmail.toLowerCase());
        referral.setRefereeName(refereeName);
        referral.setStatus(ReferralStatus.PENDING);
        referral.setReferralCode(generateReferralCode());

        referral = referralRepository.save(referral);
        logger.info("Created referral {} for user {} referring {}", referral.getReferralCode(),
                referrer.getEmail(), refereeEmail);

        // Send notification email to info@ottero.com.au
        sendReferralNotificationEmail(referrer, referral);

        return ReferralMapper.INSTANCE.toDto(referral);
    }

    public List<ReferralDto> getReferralsByUser(String userExternalId) {
        User referrer = userRepository.findByExternalId(userExternalId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Referral> referrals = referralRepository.findByReferrerOrderByCreatedDateDesc(referrer);
        return ReferralMapper.INSTANCE.toDtoList(referrals);
    }

    /**
     * Called when a new user signs up. Checks if their email was referred
     * and applies the discount to the referrer.
     */
    @Transactional
    public void processReferralOnSignup(String newUserEmail) {
        Optional<Referral> referralOpt = referralRepository.findByRefereeEmailAndStatus(
                newUserEmail.toLowerCase(), ReferralStatus.PENDING);

        if (referralOpt.isEmpty()) {
            logger.debug("No pending referral found for email: {}", newUserEmail);
            return;
        }

        Referral referral = referralOpt.get();
        User referrer = referral.getReferrer();

        logger.info("Found pending referral for {} - referrer: {}", newUserEmail, referrer.getEmail());

        // Update status to SIGNED_UP
        referral.setStatus(ReferralStatus.SIGNED_UP);
        referralRepository.save(referral);

        // Apply discount to referrer's subscription
        if (referrer.getStripeSubscriptionId() != null && !referrer.getStripeSubscriptionId().isEmpty()) {
            try {
                stripeService.applyReferralDiscount(referrer.getStripeSubscriptionId());
                referral.setStatus(ReferralStatus.DISCOUNT_APPLIED);
                referralRepository.save(referral);
                logger.info("Applied referral discount to user {} subscription", referrer.getEmail());

                // Send success email to referrer
                sendReferralSuccessEmail(referrer, referral);
            } catch (StripeException e) {
                logger.error("Failed to apply referral discount for user {}: {}",
                        referrer.getEmail(), e.getMessage());
                // Keep status as SIGNED_UP so it can be retried or handled manually
            }
        } else {
            logger.info("Referrer {} has no active subscription, discount will be applied when they subscribe",
                    referrer.getEmail());
            // Send notification email to info so they can track
            sendReferralPendingDiscountEmail(referrer, referral);
        }
    }

    private String generateReferralCode() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void sendReferralNotificationEmail(User referrer, Referral referral) {
        String subject = "New Referral Submitted - " + referral.getReferralCode();
        String body = "A new referral has been submitted.\n\n" +
                "Referral Code: " + referral.getReferralCode() + "\n" +
                "Referrer: " + referrer.getName() + " " + referrer.getSurname() + " (" + referrer.getEmail() + ")\n" +
                "Referee Email: " + referral.getRefereeEmail() + "\n" +
                "Referee Name: " + (referral.getRefereeName() != null ? referral.getRefereeName() : "Not provided") + "\n" +
                "Status: " + referral.getStatus() + "\n" +
                "Date: " + referral.getCreatedDate() + "\n\n" +
                "When the referee signs up, a discount will be automatically applied to the referrer's subscription.";

        try {
            emailService.sendEmail("info@ottero.com.au", subject, body);
        } catch (Exception e) {
            logger.error("Failed to send referral notification email: {}", e.getMessage());
        }
    }

    private void sendReferralSuccessEmail(User referrer, Referral referral) {
        String referrerName = referrer.getName() != null ? referrer.getName() : "Valued Customer";
        String subject = "Your Friend Signed Up - Free Month Applied!";
        String body = "Dear " + referrerName + ",\n\n" +
                "Great news! Your friend (" + referral.getRefereeEmail() + ") has signed up for Ottero.\n\n" +
                "As a thank you for your referral, we've applied a 100% discount to your next billing cycle. " +
                "You'll get next month free!\n\n" +
                "Keep referring friends to earn more free months.\n\n" +
                "Best regards,\n" +
                "The Ottero Team";

        try {
            emailService.sendEmail(referrer.getEmail(), subject, body);
        } catch (Exception e) {
            logger.error("Failed to send referral success email to {}: {}", referrer.getEmail(), e.getMessage());
        }
    }

    private void sendReferralPendingDiscountEmail(User referrer, Referral referral) {
        String subject = "Referral Signup - Discount Pending (No Active Subscription)";
        String body = "A referred user has signed up, but the referrer has no active subscription.\n\n" +
                "Referral Code: " + referral.getReferralCode() + "\n" +
                "Referrer: " + referrer.getName() + " " + referrer.getSurname() + " (" + referrer.getEmail() + ")\n" +
                "Referee Email: " + referral.getRefereeEmail() + "\n\n" +
                "The discount will need to be applied manually when the referrer subscribes, " +
                "or you can reach out to them about subscribing.";

        try {
            emailService.sendEmail("info@ottero.com.au", subject, body);
        } catch (Exception e) {
            logger.error("Failed to send referral pending discount email: {}", e.getMessage());
        }
    }
}

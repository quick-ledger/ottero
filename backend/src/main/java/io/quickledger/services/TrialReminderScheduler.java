package io.quickledger.services;

import io.quickledger.entities.User;
import io.quickledger.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Scheduled service to send trial ending reminder emails.
 * Runs daily to check for users whose trial ends in 3 days.
 */
@Service
public class TrialReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TrialReminderScheduler.class);

    private final UserRepository userRepository;
    private final EmailService emailService;

    public TrialReminderScheduler(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Scheduled task that runs daily at 9:00 AM to send trial ending reminders.
     * Sends emails to users whose trial ends in exactly 3 days and haven't received
     * a reminder yet.
     */
    @Scheduled(cron = "0 0 9 * * *") // Run daily at 9:00 AM
    public void sendTrialEndingReminders() {
        logger.info("Starting trial ending reminder check...");

        try {
            LocalDateTime now = LocalDateTime.now();

            // Find users who are trialing and whose trial ends in approximately 3 days
            // We check for trials ending between 2.5 and 3.5 days from now to account for
            // timing
            LocalDateTime startWindow = now.plusDays(2).plusHours(12);
            LocalDateTime endWindow = now.plusDays(3).plusHours(12);

            Iterable<User> users = userRepository.findAll();
            int remindersSent = 0;

            for (User user : users) {
                // Check if user is trialing, has a trial end date, and hasn't received reminder
                // yet
                if ("trialing".equalsIgnoreCase(user.getSubscriptionStatus()) &&
                        user.getTrialEndDate() != null &&
                        (user.getTrialReminderSent() == null || !user.getTrialReminderSent()) &&
                        user.getTrialEndDate().isAfter(startWindow) &&
                        user.getTrialEndDate().isBefore(endWindow)) {

                    try {
                        String userName = user.getName() != null ? user.getName() : "Valued Customer";
                        String planName = user.getSubscriptionPlan() != null ? user.getSubscriptionPlan() : "Premium";

                        emailService.sendTrialEndingSoonEmail(user.getEmail(), userName, planName);

                        // Mark reminder as sent
                        user.setTrialReminderSent(true);
                        userRepository.save(user);

                        remindersSent++;
                        logger.info("Sent trial ending reminder to user: {} ({}), trial ends: {}",
                                user.getExternalId(), user.getEmail(), user.getTrialEndDate());
                    } catch (Exception e) {
                        logger.error("Failed to send trial ending reminder to user: {}", user.getExternalId(), e);
                    }
                }
            }

            logger.info("Trial ending reminder check completed. Reminders sent: {}", remindersSent);
        } catch (Exception e) {
            logger.error("Error in trial ending reminder scheduler", e);
        }
    }
}

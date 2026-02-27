package io.quickledger.services;

import io.quickledger.entities.invoice.Invoice;
import io.quickledger.entities.invoice.InvoiceReminder.ReminderType;
import io.quickledger.repositories.InvoiceRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class InvoiceReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceReminderScheduler.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceReminderService invoiceReminderService;

    public InvoiceReminderScheduler(InvoiceRepository invoiceRepository,
                                     InvoiceReminderService invoiceReminderService) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceReminderService = invoiceReminderService;
    }

    @Scheduled(cron = "0 0 8 * * *") // Daily at 8 AM
    @SchedulerLock(name = "sendInvoicePaymentReminders", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void sendPaymentReminders() {
        logger.info("Starting invoice payment reminder check...");

        try {
            LocalDate today = LocalDate.now();
            List<Invoice> sentInvoices = invoiceRepository.findInvoicesForReminders();
            int remindersSent = 0;

            for (Invoice invoice : sentInvoices) {
                try {
                    LocalDate dueDate = parseDate(invoice.getDueDate());
                    if (dueDate == null) {
                        continue;
                    }

                    ReminderType reminderType = determineReminderType(today, dueDate);
                    if (reminderType != null) {
                        boolean sent = invoiceReminderService.sendReminderIfNotAlreadySent(invoice, reminderType);
                        if (sent) {
                            remindersSent++;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing reminder for invoice {}: {}",
                            invoice.getId(), e.getMessage());
                }
            }

            logger.info("Invoice payment reminder check completed. Reminders sent: {}", remindersSent);
        } catch (Exception e) {
            logger.error("Error in invoice reminder scheduler", e);
        }
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            logger.warn("Unable to parse due date: {}", dateString);
            return null;
        }
    }

    private ReminderType determineReminderType(LocalDate today, LocalDate dueDate) {
        long daysDiff = ChronoUnit.DAYS.between(today, dueDate);

        if (daysDiff == 3) {
            return ReminderType.UPCOMING_3_DAYS;
        }
        if (daysDiff == -7) {
            return ReminderType.OVERDUE_7_DAYS;
        }

        return null;
    }
}

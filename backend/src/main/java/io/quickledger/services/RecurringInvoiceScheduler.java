package io.quickledger.services;

import io.quickledger.entities.invoice.Invoice;
import io.quickledger.entities.invoice.InvoiceItem;
import io.quickledger.repositories.InvoiceRepository;
import io.quickledger.repositories.SequenceConfigRepository;
import io.quickledger.entities.SequenceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecurringInvoiceScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RecurringInvoiceScheduler.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final SequenceConfigRepository sequenceConfigRepository;

    public RecurringInvoiceScheduler(InvoiceRepository invoiceRepository,
                                      InvoiceService invoiceService,
                                      SequenceConfigRepository sequenceConfigRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceService = invoiceService;
        this.sequenceConfigRepository = sequenceConfigRepository;
    }

    @Scheduled(cron = "0 0 6 * * *") // Daily at 6 AM
    @SchedulerLock(name = "generateRecurringInvoices", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    @Transactional
    public void generateRecurringInvoices() {
        logger.info("Starting recurring invoice generation...");

        try {
            LocalDate today = LocalDate.now();
            List<Invoice> recurringInvoices = invoiceRepository.findRecurringInvoicesDueForGeneration(today);
            int invoicesGenerated = 0;

            for (Invoice templateInvoice : recurringInvoices) {
                try {
                    Invoice newInvoice = generateInvoiceFromTemplate(templateInvoice);
                    invoiceRepository.save(newInvoice);

                    // Update the next recurring date on the template
                    LocalDate nextDate = calculateNextRecurringDate(
                            templateInvoice.getRecurringFrequency(),
                            templateInvoice.getNextRecurringDate()
                    );
                    templateInvoice.setNextRecurringDate(nextDate);
                    invoiceRepository.save(templateInvoice);

                    // Auto-send if configured
                    if (Boolean.TRUE.equals(templateInvoice.getRecurringAutoSend())) {
                        try {
                            invoiceService.sendInvoice(newInvoice.getCompany().getId(), newInvoice.getId());
                            logger.info("Auto-sent recurring invoice #{} for company {}",
                                    newInvoice.getInvoiceNumber(), newInvoice.getCompany().getId());
                        } catch (Exception e) {
                            logger.error("Failed to auto-send recurring invoice #{}: {}",
                                    newInvoice.getInvoiceNumber(), e.getMessage());
                        }
                    }

                    invoicesGenerated++;
                    logger.info("Generated recurring invoice #{} from template #{} for company {}",
                            newInvoice.getInvoiceNumber(),
                            templateInvoice.getInvoiceNumber(),
                            templateInvoice.getCompany().getId());

                } catch (Exception e) {
                    logger.error("Failed to generate recurring invoice from template #{}: {}",
                            templateInvoice.getInvoiceNumber(), e.getMessage(), e);
                }
            }

            logger.info("Recurring invoice generation completed. Generated {} invoices.", invoicesGenerated);
        } catch (Exception e) {
            logger.error("Error in recurring invoice scheduler", e);
        }
    }

    private Invoice generateInvoiceFromTemplate(Invoice template) {
        Invoice newInvoice = new Invoice();

        // Copy basic details
        newInvoice.setCompany(template.getCompany());
        newInvoice.setClient(template.getClient());
        newInvoice.setUser(template.getUser());
        newInvoice.setStatus(Invoice.InvoiceStatus.DRAFT);

        // Generate new invoice number
        newInvoice.setInvoiceNumber(generateInvoiceSequence(template.getCompany().getId()));

        // Set new dates
        LocalDate invoiceDate = LocalDate.now();
        newInvoice.setInvoiceDate(invoiceDate.toString());
        newInvoice.setDueDate(invoiceDate.plusDays(14).toString()); // Default 14 days

        // Copy pricing details
        newInvoice.setTotalPrice(template.getTotalPrice());
        newInvoice.setGst(template.getGst());
        newInvoice.setDiscountType(template.getDiscountType());
        newInvoice.setDiscountValue(template.getDiscountValue());
        newInvoice.setNotes(template.getNotes());

        // Link to parent template
        newInvoice.setParentInvoiceId(template.getId());

        // This invoice is NOT recurring (it's a generated instance)
        newInvoice.setIsRecurring(false);

        // Copy line items
        List<InvoiceItem> newItems = new ArrayList<>();
        if (template.getInvoiceItems() != null) {
            for (InvoiceItem templateItem : template.getInvoiceItems()) {
                InvoiceItem newItem = new InvoiceItem();
                newItem.setInvoice(newInvoice);
                newItem.setItemDescription(templateItem.getItemDescription());
                newItem.setQuantity(templateItem.getQuantity());
                newItem.setPrice(templateItem.getPrice());
                newItem.setTotal(templateItem.getTotal());
                newItem.setGst(templateItem.getGst());
                newItem.setCode(templateItem.getCode());
                newItem.setItemOrder(templateItem.getItemOrder());
                newItems.add(newItem);
            }
        }
        newInvoice.setInvoiceItems(newItems);

        return newInvoice;
    }

    private String generateInvoiceSequence(Long companyId) {
        Optional<SequenceConfig> sequenceConfigOptional = sequenceConfigRepository
                .findByEntityTypeAndCompanyIdForUpdate(SequenceConfig.EntityType.INVOICE, companyId);

        if (!sequenceConfigOptional.isPresent()) {
            throw new IllegalStateException("Sequence configuration not found for INVOICE and company ID: " + companyId);
        }

        SequenceConfig sequenceConfig = sequenceConfigOptional.get();
        Integer currentNumber = sequenceConfig.getCurrentNumber();
        int newInvoiceSequence = currentNumber + 1;

        String newInvoiceNumber = io.quickledger.utils.SequenceNumberFormatter.format(
                sequenceConfig.getPrefix(),
                newInvoiceSequence,
                sequenceConfig.getPostfix(),
                sequenceConfig.getNumberPadding(),
                LocalDate.now());

        sequenceConfig.setCurrentNumber(newInvoiceSequence);
        sequenceConfigRepository.save(sequenceConfig);

        return newInvoiceNumber;
    }

    public static LocalDate calculateNextRecurringDate(Invoice.RecurringFrequency frequency, LocalDate fromDate) {
        if (fromDate == null) {
            fromDate = LocalDate.now();
        }

        return switch (frequency) {
            case WEEKLY -> fromDate.plusWeeks(1);
            case BIWEEKLY -> fromDate.plusWeeks(2);
            case MONTHLY -> fromDate.plusMonths(1);
            case QUARTERLY -> fromDate.plusMonths(3);
            case ANNUALLY -> fromDate.plusYears(1);
        };
    }
}

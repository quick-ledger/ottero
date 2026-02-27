package io.quickledger.repositories;

import io.quickledger.entities.invoice.InvoiceReminder;
import io.quickledger.entities.invoice.InvoiceReminder.ReminderType;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface InvoiceReminderRepository extends CrudRepository<InvoiceReminder, Long> {

    boolean existsByInvoiceIdAndReminderTypeAndSuccessTrue(Long invoiceId, ReminderType reminderType);

    Optional<InvoiceReminder> findTopByInvoiceIdOrderBySentAtDesc(Long invoiceId);
}

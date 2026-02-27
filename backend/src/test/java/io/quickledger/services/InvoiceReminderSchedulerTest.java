package io.quickledger.services;

import io.quickledger.entities.Client;
import io.quickledger.entities.Company;
import io.quickledger.entities.invoice.Invoice;
import io.quickledger.entities.invoice.InvoiceReminder.ReminderType;
import io.quickledger.repositories.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceReminderSchedulerTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceReminderService invoiceReminderService;

    private InvoiceReminderScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new InvoiceReminderScheduler(invoiceRepository, invoiceReminderService);
    }

    private Invoice createTestInvoice(Long id, String dueDate) {
        Company company = new Company();
        company.setId(1L);
        company.setName("Test Company");

        Client client = new Client();
        client.setId(1L);
        client.setEmail("client@test.com");
        client.setContactName("John");

        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setInvoiceNumber("INV-" + id);
        invoice.setCompany(company);
        invoice.setClient(client);
        invoice.setDueDate(dueDate);
        invoice.setTotalPrice(new BigDecimal("100.00"));
        invoice.setStatus(Invoice.InvoiceStatus.SENT);

        return invoice;
    }

    @Test
    void sendPaymentReminders_sendsUpcomingReminder3DaysBefore() {
        LocalDate today = LocalDate.now();
        String dueDateIn3Days = today.plusDays(3).toString();
        Invoice invoice = createTestInvoice(1L, dueDateIn3Days);

        when(invoiceRepository.findInvoicesForReminders()).thenReturn(List.of(invoice));
        when(invoiceReminderService.sendReminderIfNotAlreadySent(invoice, ReminderType.UPCOMING_3_DAYS))
                .thenReturn(true);

        scheduler.sendPaymentReminders();

        verify(invoiceReminderService).sendReminderIfNotAlreadySent(invoice, ReminderType.UPCOMING_3_DAYS);
    }

    @Test
    void sendPaymentReminders_sendsOverdueReminder7DaysAfter() {
        LocalDate today = LocalDate.now();
        String dueDate7DaysAgo = today.minusDays(7).toString();
        Invoice invoice = createTestInvoice(2L, dueDate7DaysAgo);

        when(invoiceRepository.findInvoicesForReminders()).thenReturn(List.of(invoice));
        when(invoiceReminderService.sendReminderIfNotAlreadySent(invoice, ReminderType.OVERDUE_7_DAYS))
                .thenReturn(true);

        scheduler.sendPaymentReminders();

        verify(invoiceReminderService).sendReminderIfNotAlreadySent(invoice, ReminderType.OVERDUE_7_DAYS);
    }

    @Test
    void sendPaymentReminders_skipsInvoiceWithNoMatchingReminderType() {
        LocalDate today = LocalDate.now();
        String dueDateIn5Days = today.plusDays(5).toString(); // Not 3 days, not -7 days
        Invoice invoice = createTestInvoice(3L, dueDateIn5Days);

        when(invoiceRepository.findInvoicesForReminders()).thenReturn(List.of(invoice));

        scheduler.sendPaymentReminders();

        verify(invoiceReminderService, never()).sendReminderIfNotAlreadySent(any(), any());
    }

    @Test
    void sendPaymentReminders_skipsInvoiceWithInvalidDueDate() {
        Invoice invoice = createTestInvoice(4L, "invalid-date");

        when(invoiceRepository.findInvoicesForReminders()).thenReturn(List.of(invoice));

        scheduler.sendPaymentReminders();

        verify(invoiceReminderService, never()).sendReminderIfNotAlreadySent(any(), any());
    }

    @Test
    void sendPaymentReminders_skipsInvoiceWithNullDueDate() {
        Invoice invoice = createTestInvoice(5L, null);

        when(invoiceRepository.findInvoicesForReminders()).thenReturn(List.of(invoice));

        scheduler.sendPaymentReminders();

        verify(invoiceReminderService, never()).sendReminderIfNotAlreadySent(any(), any());
    }

    @Test
    void sendPaymentReminders_continuesAfterExceptionOnSingleInvoice() {
        LocalDate today = LocalDate.now();
        Invoice invoice1 = createTestInvoice(1L, today.plusDays(3).toString());
        Invoice invoice2 = createTestInvoice(2L, today.plusDays(3).toString());

        when(invoiceRepository.findInvoicesForReminders()).thenReturn(List.of(invoice1, invoice2));
        when(invoiceReminderService.sendReminderIfNotAlreadySent(invoice1, ReminderType.UPCOMING_3_DAYS))
                .thenThrow(new RuntimeException("Unexpected error"));
        when(invoiceReminderService.sendReminderIfNotAlreadySent(invoice2, ReminderType.UPCOMING_3_DAYS))
                .thenReturn(true);

        scheduler.sendPaymentReminders();

        verify(invoiceReminderService).sendReminderIfNotAlreadySent(invoice1, ReminderType.UPCOMING_3_DAYS);
        verify(invoiceReminderService).sendReminderIfNotAlreadySent(invoice2, ReminderType.UPCOMING_3_DAYS);
    }
}

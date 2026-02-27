package io.quickledger.services;

import io.quickledger.entities.Client;
import io.quickledger.entities.Company;
import io.quickledger.entities.invoice.Invoice;
import io.quickledger.entities.invoice.InvoiceReminder.ReminderType;
import io.quickledger.repositories.InvoiceReminderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceReminderServiceTest {

    @Mock
    private InvoiceReminderRepository invoiceReminderRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private EmailService emailService;

    @Mock
    private TempTokenService tempTokenService;

    @Mock
    private StripeService stripeService;

    private InvoiceReminderService invoiceReminderService;

    private Invoice testInvoice;
    private Client testClient;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        invoiceReminderService = new InvoiceReminderService(
                invoiceReminderRepository,
                invoiceService,
                emailService,
                tempTokenService,
                stripeService
        );

        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Company");
        testCompany.setEmail("company@test.com");
        testCompany.setPhone("1234567890");

        testClient = new Client();
        testClient.setId(1L);
        testClient.setContactName("John");
        testClient.setContactSurname("Doe");
        testClient.setEmail("client@test.com");

        testInvoice = new Invoice();
        testInvoice.setId(1L);
        testInvoice.setInvoiceNumber("INV-001");
        testInvoice.setCompany(testCompany);
        testInvoice.setClient(testClient);
        testInvoice.setDueDate("2026-03-01");
        testInvoice.setTotalPrice(new BigDecimal("100.00"));
        testInvoice.setStatus(Invoice.InvoiceStatus.SENT);
    }

    @Test
    void sendReminderIfNotAlreadySent_skipsDuplicate() {
        when(invoiceReminderRepository.existsByInvoiceIdAndReminderTypeAndSuccessTrue(1L, ReminderType.UPCOMING_3_DAYS))
                .thenReturn(true);

        boolean result = invoiceReminderService.sendReminderIfNotAlreadySent(testInvoice, ReminderType.UPCOMING_3_DAYS);

        assertFalse(result);
        verify(emailService, never()).sendEmailWithAttachment(any(), any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void sendReminderIfNotAlreadySent_skipsNullClientEmail() {
        testClient.setEmail(null);
        when(invoiceReminderRepository.existsByInvoiceIdAndReminderTypeAndSuccessTrue(1L, ReminderType.UPCOMING_3_DAYS))
                .thenReturn(false);

        boolean result = invoiceReminderService.sendReminderIfNotAlreadySent(testInvoice, ReminderType.UPCOMING_3_DAYS);

        assertFalse(result);
        verify(emailService, never()).sendEmailWithAttachment(any(), any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void sendReminderIfNotAlreadySent_skipsBlankClientEmail() {
        testClient.setEmail("   ");
        when(invoiceReminderRepository.existsByInvoiceIdAndReminderTypeAndSuccessTrue(1L, ReminderType.UPCOMING_3_DAYS))
                .thenReturn(false);

        boolean result = invoiceReminderService.sendReminderIfNotAlreadySent(testInvoice, ReminderType.UPCOMING_3_DAYS);

        assertFalse(result);
        verify(emailService, never()).sendEmailWithAttachment(any(), any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void sendReminderIfNotAlreadySent_sendsUpcomingReminder() {
        when(invoiceReminderRepository.existsByInvoiceIdAndReminderTypeAndSuccessTrue(1L, ReminderType.UPCOMING_3_DAYS))
                .thenReturn(false);
        when(invoiceService.downloadInvoicePdf(1L, 1L)).thenReturn(new byte[]{1, 2, 3});
        when(tempTokenService.getOrCreateInvoiceToken(eq(1L), eq(1L), eq(1L), anyLong())).thenReturn("test-token");

        boolean result = invoiceReminderService.sendReminderIfNotAlreadySent(testInvoice, ReminderType.UPCOMING_3_DAYS);

        assertTrue(result);
        verify(emailService).sendEmailWithAttachment(
                eq("client@test.com"),
                eq("Payment Reminder: Invoice #INV-001 due in 3 days"),
                any(),
                any(),
                eq("invoice-INV-001.pdf"),
                eq("Test Company"),
                eq("company@test.com"),
                eq(true)
        );
        verify(invoiceReminderRepository).save(any());
    }

    @Test
    void sendReminderIfNotAlreadySent_sendsOverdueReminder() {
        when(invoiceReminderRepository.existsByInvoiceIdAndReminderTypeAndSuccessTrue(1L, ReminderType.OVERDUE_7_DAYS))
                .thenReturn(false);
        when(invoiceService.downloadInvoicePdf(1L, 1L)).thenReturn(new byte[]{1, 2, 3});
        when(tempTokenService.getOrCreateInvoiceToken(eq(1L), eq(1L), eq(1L), anyLong())).thenReturn("test-token");

        boolean result = invoiceReminderService.sendReminderIfNotAlreadySent(testInvoice, ReminderType.OVERDUE_7_DAYS);

        assertTrue(result);
        verify(emailService).sendEmailWithAttachment(
                eq("client@test.com"),
                eq("Payment Overdue: Invoice #INV-001 from Test Company"),
                any(),
                any(),
                eq("invoice-INV-001.pdf"),
                eq("Test Company"),
                eq("company@test.com"),
                eq(true)
        );
        verify(invoiceReminderRepository).save(any());
    }

    @Test
    void sendReminderIfNotAlreadySent_recordsFailureOnException() {
        when(invoiceReminderRepository.existsByInvoiceIdAndReminderTypeAndSuccessTrue(1L, ReminderType.UPCOMING_3_DAYS))
                .thenReturn(false);
        when(invoiceService.downloadInvoicePdf(1L, 1L)).thenThrow(new RuntimeException("PDF generation failed"));

        boolean result = invoiceReminderService.sendReminderIfNotAlreadySent(testInvoice, ReminderType.UPCOMING_3_DAYS);

        assertFalse(result);
        verify(invoiceReminderRepository).save(argThat(reminder ->
            !reminder.getSuccess() && reminder.getErrorMessage().contains("PDF generation failed")
        ));
    }
}

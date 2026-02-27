package io.quickledger.entities.invoice;

import io.quickledger.entities.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_reminders", indexes = {
        @Index(name = "idx_invoice_reminder_type", columnList = "invoice_id, reminder_type"),
        @Index(name = "idx_reminder_sent_at", columnList = "sent_at")
})
public class InvoiceReminder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "reminder_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReminderType reminderType;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "recipient_email", nullable = false, length = 500)
    private String recipientEmail;

    @Column(name = "success", nullable = false)
    private Boolean success = true;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    public enum ReminderType {
        UPCOMING_3_DAYS,
        OVERDUE_7_DAYS
    }

    public InvoiceReminder() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public ReminderType getReminderType() {
        return reminderType;
    }

    public void setReminderType(ReminderType reminderType) {
        this.reminderType = reminderType;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

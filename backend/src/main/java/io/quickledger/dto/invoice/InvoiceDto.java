package io.quickledger.dto.invoice;

import io.quickledger.entities.invoice.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceDto {
    private Long id;
    private Long companyId;
    private Long userId;
    private String clientFirstname;
    private String clientLastname;
    private String clientEmail;
    private String clientEntityName;
    private String clientPhone;
    private Long clientId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private Invoice.InvoiceStatus status;
    private BigDecimal totalPrice;
    private BigDecimal gst;
    private List<InvoiceItemDto> invoiceItems;
    private String notes;
    private BigDecimal discountValue; // New field for discount value
    private Invoice.DiscountType discountType; // New field for discount type
    private Long quoteId;
    private String quoteNumber;
    private String paymentLink;

    // Recurring invoice fields
    private Boolean isRecurring;
    private Invoice.RecurringFrequency recurringFrequency;
    private LocalDate recurringEndDate;
    private Boolean recurringAutoSend;
    private LocalDate nextRecurringDate;
    private Long parentInvoiceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getClientFirstname() {
        return clientFirstname;
    }

    public void setClientFirstname(String clientFirstname) {
        this.clientFirstname = clientFirstname;
    }

    public String getClientLastname() {
        return clientLastname;
    }

    public void setClientLastname(String clientLastname) {
        this.clientLastname = clientLastname;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientEntityName() {
        return clientEntityName;
    }

    public void setClientEntityName(String clientEntityName) {
        this.clientEntityName = clientEntityName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Invoice.InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(Invoice.InvoiceStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getGst() {
        return gst;
    }

    public void setGst(BigDecimal gst) {
        this.gst = gst;
    }

    public List<InvoiceItemDto> getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(List<InvoiceItemDto> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public Invoice.DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(Invoice.DiscountType discountType) {
        this.discountType = discountType;
    }

    public Long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(Long quoteId) {
        this.quoteId = quoteId;
    }

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public void setQuoteNumber(String quoteNumber) {
        this.quoteNumber = quoteNumber;
    }

    public String getPaymentLink() {
        return paymentLink;
    }

    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public Invoice.RecurringFrequency getRecurringFrequency() {
        return recurringFrequency;
    }

    public void setRecurringFrequency(Invoice.RecurringFrequency recurringFrequency) {
        this.recurringFrequency = recurringFrequency;
    }

    public LocalDate getRecurringEndDate() {
        return recurringEndDate;
    }

    public void setRecurringEndDate(LocalDate recurringEndDate) {
        this.recurringEndDate = recurringEndDate;
    }

    public Boolean getRecurringAutoSend() {
        return recurringAutoSend;
    }

    public void setRecurringAutoSend(Boolean recurringAutoSend) {
        this.recurringAutoSend = recurringAutoSend;
    }

    public LocalDate getNextRecurringDate() {
        return nextRecurringDate;
    }

    public void setNextRecurringDate(LocalDate nextRecurringDate) {
        this.nextRecurringDate = nextRecurringDate;
    }

    public Long getParentInvoiceId() {
        return parentInvoiceId;
    }

    public void setParentInvoiceId(Long parentInvoiceId) {
        this.parentInvoiceId = parentInvoiceId;
    }
}

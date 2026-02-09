package io.quickledger.dto.quote;

import io.quickledger.entities.quote.Quote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class QuoteDto {
    private Long id;
    private Long companyId;
    private Long userId;
    private String clientFirstname;
    private String clientLastname;
    private String clientEmail;
    private String clientEntityName;
    private String clientPhone;
    private Long clientId;
    private String quoteNumber;
    private LocalDate quoteDate;
    private LocalDate expiryDate;
    private Quote.QuoteStatus status;
    private BigDecimal totalPrice;
    private BigDecimal gst;
    private List<QuoteItemDto> quoteItems;
    private String notes;
    private String clientNotes;
    private BigDecimal discountValue; // New field for discount value
    private Quote.DiscountType discountType; // New field for discount type
    private Integer quoteRevision; // New field for quote revision
    private List<QuoteAttachmentDto> attachments;
    private List<io.quickledger.dto.invoice.InvoiceDto> relatedInvoices;

    // Getters and Setters

    public List<io.quickledger.dto.invoice.InvoiceDto> getRelatedInvoices() {
        return relatedInvoices;
    }

    public void setRelatedInvoices(List<io.quickledger.dto.invoice.InvoiceDto> relatedInvoices) {
        this.relatedInvoices = relatedInvoices;
    }

    public List<QuoteAttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<QuoteAttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public Integer getQuoteRevision() {
        return quoteRevision;
    }

    public void setQuoteRevision(Integer quoteRevision) {
        this.quoteRevision = quoteRevision;
    }

    public String getClientEntityName() {
        return clientEntityName;
    }

    public void setClientEntityName(String clientEntityName) {
        this.clientEntityName = clientEntityName;
    }

    public String getClientLastname() {
        return clientLastname;
    }

    public void setClientLastname(String clientLastname) {
        this.clientLastname = clientLastname;
    }

    public String getClientFirstname() {
        return clientFirstname;
    }

    public void setClientFirstname(String clientFirstname) {
        this.clientFirstname = clientFirstname;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public Quote.DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(Quote.DiscountType discountType) {
        this.discountType = discountType;
    }

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

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public void setQuoteNumber(String quoteNumber) {
        this.quoteNumber = quoteNumber;
    }

    public LocalDate getQuoteDate() {
        return quoteDate;
    }

    public void setQuoteDate(LocalDate quoteDate) {
        this.quoteDate = quoteDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Quote.QuoteStatus getStatus() {
        return status;
    }

    public void setStatus(Quote.QuoteStatus status) {
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

    public List<QuoteItemDto> getQuoteItems() {
        return quoteItems;
    }

    public void setQuoteItems(List<QuoteItemDto> quoteItems) {
        this.quoteItems = quoteItems;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getClientNotes() {
        return clientNotes;
    }

    public void setClientNotes(String clientNotes) {
        this.clientNotes = clientNotes;
    }

}

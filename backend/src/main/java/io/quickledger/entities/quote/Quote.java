package io.quickledger.entities.quote;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Client;
import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.entities.invoice.Invoice;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// make sure field names of quote and invoice match as much as possible.

@Entity
@Table(name = "quotes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "quote_revision", "quote_number", "company_id" })
})
public class Quote extends BaseEntity {
    public Quote() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private User user;

    // we do NOT always want to load all the quote items when we load a quote
    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuoteItem> quoteItems;

    @OneToMany(mappedBy = "quote", fetch = FetchType.LAZY)
    private List<QuoteAttachment> attachments;

    @OneToMany(mappedBy = "quote", fetch = FetchType.LAZY)
    private List<Invoice> invoices;

    @Column(name = "quote_number", length = 500, nullable = false)
    private String quoteNumber;

    @Column(name = "quote_date", nullable = false)
    private LocalDate quoteDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "status", length = 500, nullable = false)
    private QuoteStatus status;

    public enum QuoteStatus {
        PENDING("Pending"), // Draft - not yet sent to customer
        SENT("Sent"), // Sent to customer, awaiting response
        ACCEPTED("Accepted"), // Customer accepted
        REJECTED("Rejected"), // Customer rejected
        CANCELLED("Cancelled"); // Operator cancelled

        // I used to have a separate field called "Customer Response", which was
        // rejected and accepted
        // but that really didn't work so I'm back at this quote status.

        private final String description;

        QuoteStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Column(name = "quote_revision")
    private Integer quoteRevision;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "gst", nullable = false)
    private BigDecimal gst;

    @Column(name = "notes", nullable = true, length = 4000)
    private String notes;

    @Column(name = "client_notes", nullable = true, length = 4000)
    private String clientNotes;

    @Column(name = "discount_value", nullable = true, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "discount_type", nullable = true)
    private String discountType;

    public enum DiscountType {
        PERCENT, DOLLAR;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
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

    public QuoteStatus getStatus() {
        return status;
    }

    public void setStatus(QuoteStatus status) {
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

    public List<QuoteItem> getQuoteItems() {
        return quoteItems;
    }

    public void setQuoteItems(List<QuoteItem> quoteItems) {
        this.quoteItems = quoteItems;
    }

    public List<QuoteAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<QuoteAttachment> attachments) {
        this.attachments = attachments;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getQuoteRevision() {
        return quoteRevision;
    }

    public void setQuoteRevision(Integer quoteRevision) {
        this.quoteRevision = quoteRevision;
    }

    public String getClientNotes() {
        return clientNotes;
    }

    public void setClientNotes(String clientNotes) {
        this.clientNotes = clientNotes;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "id=" + id +
                ", company=" + company +
                ", user=" + user +
                ", quoteItems=" + quoteItems +
                ", quoteNumber='" + quoteNumber + '\'' +
                ", quoteDate='" + quoteDate + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                ", gst=" + gst +
                ", notes='" + notes + '\'' +
                ", discountValue=" + discountValue +
                ", discountType=" + discountType +
                '}';
    }
}

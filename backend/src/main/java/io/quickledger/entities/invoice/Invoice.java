package io.quickledger.entities.invoice;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Client;
import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.entities.quote.Quote;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

// make sure field names of quote and invoice match as much as possible.

@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {
    // default constructor
    public Invoice() {
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
    @JoinColumn(name = "quote_id", referencedColumnName = "id")
    private Quote quote;// related quote?

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvoiceItem> invoiceItems;

    @Column(name = "invoice_number", length = 500, nullable = false)
    private String invoiceNumber;

    @Column(name = "invoice_date", length = 500, nullable = false)
    private String invoiceDate;

    @Column(name = "due_date", length = 500, nullable = false)
    private String dueDate;

    @Column(name = "status", length = 500, nullable = false)
    private InvoiceStatus status;

    public enum InvoiceStatus {
        // OVERDUE is best handled as a calculated warning based on dueDate < today
        // rather than a database status
        DRAFT, SENT, PAID, CANCELLED
    }

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "gst", nullable = false)
    private BigDecimal gst;

    @Column(name = "notes", nullable = true, length = 4000)
    private String notes;

    @Column(name = "discount_value", nullable = true, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "discount_type", nullable = true)
    private DiscountType discountType;

    public enum DiscountType {
        PERCENT, DOLLAR;
    }

    // Getters and Setters

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
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

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public User getEmployee() {
        return user;
    }

    public void setEmployee(User user) {
        this.user = user;
    }

    public List<InvoiceItem> getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(List<InvoiceItem> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
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

    // toString method
    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", company=" + company + '\'' +
                ", client=" + client + '\'' +
                ", quote=" + quote + '\'' +
                ", user=" + user + '\'' +
                ", invoiceItems=" + invoiceItems + '\'' +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", invoiceDate='" + invoiceDate + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", status='" + status + '\'' +
                ", totalPrice=" + totalPrice + '\'' +
                ", gst=" + gst + '\'' +
                ", baseEntity=" + super.toString() + '\'' +
                '}';
    }

}

package io.quickledger.entities.supplier;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Company;
import jakarta.persistence.*;

/**
 * Represents a supplier/vendor that the company purchases from.
 * Used in Purchase Orders for procurement workflow.
 */
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "contact_name", length = 500)
    private String contactName;

    @Column(name = "email", length = 500)
    private String email;

    @Column(name = "phone", length = 100)
    private String phone;

    @Column(name = "address", length = 1000)
    private String address;

    @Column(name = "payment_terms", length = 500)
    private String paymentTerms; // e.g., "Net 30", "COD", "Net 14"

    @Column(name = "notes", length = 4000)
    private String notes;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive = true;

    // Getters and Setters

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}

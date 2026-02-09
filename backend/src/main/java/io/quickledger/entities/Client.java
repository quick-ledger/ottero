package io.quickledger.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "clients") // This names the table as "clients" in the database
public class Client extends BaseEntity {

    // Default constructor
    public Client() {
    }

    // Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = false)
    private Company company;

    // This is the legal name or company name of a client. It does not exist all the
    // time.
    @Column(name = "entity_name", length = 500, nullable = true)
    private String entityName;

    @Column(name = "contact_name", length = 500, nullable = false)
    private String contactName;
    @Column(name = "contact_surname", length = 500, nullable = false)
    private String contactSurname;

    /*
     * don't make this unique. husband and wife can have same email.
     * also currently we allow the same client for two companies.
     */
    @Column(name = "email", length = 500, nullable = false)
    private String email;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone; // TODO validate only number

    @Column(name = "address", length = 500, nullable = true)
    private String address;

    @Column(name = "status", length = 50, nullable = true)
    private String status;

    // client reference number
    @Column(name = "account_number", length = 50, nullable = true)
    private String accountNumber;

    // Getters and Setters

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String name) {
        this.entityName = name;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactSurname() {
        return contactSurname;
    }

    public void setContactSurname(String contactSurname) {
        this.contactSurname = contactSurname;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // toString method
    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", company=" + company +
                ", entityName='" + entityName + '\'' +
                ", contactName='" + contactName + '\'' +
                ", contactSurname='" + contactSurname + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

package io.quickledger.entities;

import io.quickledger.entities.asset.AssetGroup;
import io.quickledger.entities.product.ProductDefinition;
import io.quickledger.entities.serviceitem.ServiceItem;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "companies") // This names the table as "companies" in the database
public class Company extends BaseEntity {

    // Default constructor is required by JPA
    public Company() {
    }

    public Company(Long id) {
        this.id = id;
    }

    // Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // For auto-generated, unique primary key
    private Long id;

    // To make it bidirectional use following but we shouldn't need it
    // @ManyToMany(fetch = FetchType.LAZY)
    // @JoinTable(
    // name = "user_companies",
    // joinColumns = @JoinColumn(name = "company_id"),
    // inverseJoinColumns = @JoinColumn(name = "user_id")
    // )
    // to make it one directional shouldn't need company users
    // @ManyToMany(mappedBy = "companies", fetch = FetchType.LAZY)
    @OneToMany(mappedBy = "company")
    private Set<UserCompany> userCompanies;

    @OneToMany(mappedBy = "company")
    private List<ProductDefinition> productDefinitions;

    @OneToMany(mappedBy = "company")
    @Column(name = "asset_groups", nullable = true)
    private List<AssetGroup> assetGroups = new ArrayList<>();

    @OneToMany(mappedBy = "company")
    @Column(name = "warehouses", nullable = true)
    private List<Warehouse> warehouses;

    @OneToMany(mappedBy = "company")
    @Column(name = "service_items", nullable = true)
    private List<ServiceItem> serviceItems;

    @OneToMany(mappedBy = "company")
    private List<Client> clients;

    @Column(name = "name", nullable = false, length = 500)
    // Customizes the mapping between the field and the database column
    private String name;

    @Column(name = "industry_type", length = 500, nullable = true)
    private String IndustryType;

    @Column(name = "abn", length = 11, nullable = true)
    private String ABN;

    @Column(name = "acn", length = 9, nullable = true)
    private String ACN;

    @Column(name = "phone", length = 10, nullable = true)
    private String phone;

    @Column(name = "email", unique = false, length = 500)
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "licence_number", length = 20, nullable = true)
    private String licenceNumber;

    @Column(name = "bank_bsb", length = 6, nullable = true, unique = false)
    private String bankBsb;

    @Column(name = "bank_account", length = 9, nullable = true, unique = false)
    private String bankAccount;

    @Column(name = "images", nullable = true, columnDefinition = "MEDIUMBLOB")
    private byte[] image;

    @Column(name = "website", length = 500, nullable = true)
    private String website;

    @Column(name = "logo_content_type", length = 100, nullable = true)
    private String logoContentType;

    @Column(name = "template_config", columnDefinition = "TEXT", nullable = true)
    private String templateConfig; // JSON string storing template configuration

    @Column(name = "stripe_connected_account_id")
    private String stripeConnectedAccountId;

    @Column(name = "stripe_charges_enabled")
    private Boolean stripeChargesEnabled;

    // Subscription Fields moved to User Entity

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustryType() {
        return IndustryType;
    }

    public void setIndustryType(String industryType) {
        IndustryType = industryType;
    }

    public String getABN() {
        return ABN;
    }

    public void setABN(String ABN) {
        this.ABN = ABN;
    }

    public String getACN() {
        return ACN;
    }

    public void setACN(String ACN) {
        this.ACN = ACN;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBankBsb() {
        return bankBsb;
    }

    public void setBankBsb(String bankBsb) {
        this.bankBsb = bankBsb;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] images) {
        this.image = images;
    }

    public Set<UserCompany> getUserCompanies() {
        return userCompanies;
    }

    public void setUserCompanies(Set<UserCompany> userCompanies) {
        this.userCompanies = userCompanies;
    }

    public List<Warehouse> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(List<Warehouse> warehouses) {
        this.warehouses = warehouses;
    }

    public List<ServiceItem> getServiceItems() {
        return serviceItems;
    }

    public void setServiceItems(List<ServiceItem> serviceItems) {
        this.serviceItems = serviceItems;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<AssetGroup> getAssetGroups() {
        return assetGroups;
    }

    public void setAssetGroups(List<AssetGroup> assetGroups) {
        this.assetGroups = assetGroups;
    }

    public List<ProductDefinition> getProductDefinitions() {
        return productDefinitions;
    }

    public void setProductDefinitions(List<ProductDefinition> productDefinitions) {
        this.productDefinitions = productDefinitions;
    }

    public String getLogoContentType() {
        return logoContentType;
    }

    public void setLogoContentType(String logoContentType) {
        this.logoContentType = logoContentType;
    }

    public String getTemplateConfig() {
        return templateConfig;
    }

    public void setTemplateConfig(String templateConfig) {
        this.templateConfig = templateConfig;
    }

    public String getStripeConnectedAccountId() {
        return stripeConnectedAccountId;
    }

    public void setStripeConnectedAccountId(String stripeConnectedAccountId) {
        this.stripeConnectedAccountId = stripeConnectedAccountId;
    }

    public boolean isStripeChargesEnabled() {
        return stripeChargesEnabled != null && stripeChargesEnabled;
    }

    public void setStripeChargesEnabled(boolean stripeChargesEnabled) {
        this.stripeChargesEnabled = stripeChargesEnabled;
    }

    // toString method
    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", userCompanies=" + userCompanies +
                ", warehouses=" + warehouses +
                ", serviceItems=" + serviceItems +
                ", clients=" + clients +
                ", name='" + name + '\'' +
                ", IndustryType='" + IndustryType + '\'' +
                ", ABN='" + ABN + '\'' +
                ", ACN='" + ACN + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", bankBsb='" + bankBsb + '\'' +
                ", bankAccount='" + bankAccount + '\'' +
                ", website='" + website + '\'' +
                ", baseEntity=" + super.toString() + '\'' +
                '}';
    }
}

package io.quickledger.dto;

import io.quickledger.dto.product.ProductDefinitionDto;

import java.util.List;

public class CompanyDto {

    private Long id;
    private String name;
    private String IndustryType;
    private String ABN;
    private String ACN;
    private String phone;
    private String email;
    private String address;
    private String bankBsb;
    private String bankAccount;
    private String website;
    private String stripeConnectedAccountId;
    private Boolean stripeChargesEnabled;
    private List<ProductDefinitionDto> productDefinitions;

    // Implemented outside of DTO as it's a binary object and not a good practice to
    // keep it in DTO
    // private List<byte[]> images; //@see ImageController

    // Getters and Setters

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

    public List<ProductDefinitionDto> getProductDefinitions() {
        return productDefinitions;
    }

    public void setProductDefinitions(List<ProductDefinitionDto> productDefinitions) {
        this.productDefinitions = productDefinitions;
    }

    // public List<byte[]> getImages() {
    // return images;
    // }
    //
    // public void setImages(List<byte[]> images) {
    // this.images = images;
    // }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getStripeConnectedAccountId() {
        return stripeConnectedAccountId;
    }

    public void setStripeConnectedAccountId(String stripeConnectedAccountId) {
        this.stripeConnectedAccountId = stripeConnectedAccountId;
    }

    public Boolean getStripeChargesEnabled() {
        return stripeChargesEnabled;
    }

    public void setStripeChargesEnabled(Boolean stripeChargesEnabled) {
        this.stripeChargesEnabled = stripeChargesEnabled;
    }

    @Override
    public String toString() {
        return "CompanyDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", IndustryType='" + IndustryType + '\'' +
                ", website='" + website + '\'' +
                '}';
    }
}
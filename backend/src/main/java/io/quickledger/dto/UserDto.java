package io.quickledger.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quickledger.entities.Company;
import jakarta.persistence.Column;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class UserDto {
    @JsonIgnore
    private Long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String phone;
    private String address;
    private int age;
    private char gender;
    private String role;
    private String TFN;
    private List<byte[]> images;
    private LocalDateTime registerDateTime;
    // Add list of Companies related to user to UserDTO
    // private Set<CompanyDto> companies;
    private CompanyDto defaultCompany;

    // Subscription fields
    private String subscriptionPlan;
    private String subscriptionStatus;
    private String stripeCustomerId;
    private Boolean cancelAtPeriodEnd;

    // Getters and Setters

    public Boolean getCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public void setCancelAtPeriodEnd(Boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTFN() {
        return TFN;
    }

    public void setTFN(String TFN) {
        this.TFN = TFN;
    }

    public List<byte[]> getImages() {
        return images;
    }

    public void setImages(List<byte[]> images) {
        this.images = images;
    }

    public LocalDateTime getRegisterDateTime() {
        return registerDateTime;
    }

    public void setRegisterDateTime(LocalDateTime registerDateTime) {
        this.registerDateTime = registerDateTime;
    }

    public CompanyDto getDefaultCompany() {
        return defaultCompany;
    }

    public void setDefaultCompany(CompanyDto defaultCompany) {
        this.defaultCompany = defaultCompany;
    }

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }
}

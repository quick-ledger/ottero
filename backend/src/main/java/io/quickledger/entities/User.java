package io.quickledger.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

//we put this object in the security context. keep it light and no extra fetching from user.

@Entity
@Table(name = "users") // This names the table as "users" in the database
public class User extends BaseEntity {
    // Default constructor
    public User() {
    }

    // Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // For auto-generated, unique primary key
    private Long id;

    // TODO can we remove this from the user object? RG
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UserCompany> userCompanies;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "surname", length = 500, nullable = false)
    private String surname;

    @Column(name = "username", length = 500, nullable = true)
    // Username is optional as we will have it after Keycloak registration
    private String username;

    @Column(name = "email", unique = true, length = 500)
    private String email;

    @Column(name = "external_id", unique = true, length = 255)
    private String externalId;

    @Column(name = "phone", length = 10, nullable = true)
    private String phone;

    @Column(name = "address", length = 500, nullable = true)
    private String address;

    @Column(name = "age", length = 3, nullable = true)
    private int age;

    @Column(name = "gender", length = 1, nullable = true)
    private char gender;

    @Column(name = "role", length = 500, nullable = true)
    private String role;

    @Column(name = "status", length = 500, nullable = true)
    private String status;

    @Column(name = "tfn", length = 9, nullable = true)
    private String TFN;

    // Assume images are stored internally in DB nor externally thus not using URLs
    @ElementCollection
    @Column(name = "images", nullable = true, columnDefinition = "MEDIUMBLOB")
    private List<byte[]> images;

    // User registration datetime
    @Column(name = "registration_date")
    private LocalDateTime registerDateTime;

    @ManyToOne
    @JoinColumn(name = "default_company_id")
    private Company defaultCompany;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<UserCompany> getUserCompanies() {
        return userCompanies;
    }

    public void setUserCompanies(Set<UserCompany> userCompanies) {
        this.userCompanies = userCompanies;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Company getDefaultCompany() {
        return defaultCompany;
    }

    public void setDefaultCompany(Company defaultCompany) {
        this.defaultCompany = defaultCompany;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    // Subscription Fields
    @Column(name = "stripe_customer_id", length = 255)
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id", length = 255)
    private String stripeSubscriptionId;

    @Column(name = "subscription_plan", length = 50)
    private String subscriptionPlan; // e.g., "Basic", "Advanced"

    @Column(name = "subscription_status", length = 50)
    private String subscriptionStatus; // e.g., "active", "past_due", "canceled"

    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate; // When the trial period ends

    @Column(name = "trial_reminder_sent")
    private Boolean trialReminderSent = false; // Track if 3-day reminder was sent

    @Column(name = "cancel_at_period_end")
    private Boolean cancelAtPeriodEnd = false;

    // Getters and Setters for Subscription Fields
    public Boolean getCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public void setCancelAtPeriodEnd(Boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
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

    public LocalDateTime getTrialEndDate() {
        return trialEndDate;
    }

    public void setTrialEndDate(LocalDateTime trialEndDate) {
        this.trialEndDate = trialEndDate;
    }

    public Boolean getTrialReminderSent() {
        return trialReminderSent;
    }

    public void setTrialReminderSent(Boolean trialReminderSent) {
        this.trialReminderSent = trialReminderSent;
    }

    /**
     * Check if user has active subscription access.
     * Returns true if subscription status is "active", "trialing", or "past_due".
     * 
     * Grace Period Policy (Lenient):
     * - "active": Full paid access
     * - "trialing": Full access during trial period
     * - "past_due": Full access while Stripe retries payment (up to 2 weeks by
     * default)
     * 
     * This lenient approach provides better UX and allows Stripe's Smart Retries
     * to recover failed payments without immediately blocking users.
     * 
     * @return true if user has active access, false otherwise
     */
    public boolean hasActiveSubscription() {
        return "active".equalsIgnoreCase(subscriptionStatus) ||
                "trialing".equalsIgnoreCase(subscriptionStatus) ||
                "past_due".equalsIgnoreCase(subscriptionStatus); // ✅ Grace period during retries
    }

    /**
     * Check if user is currently in trial period.
     * 
     * @return true if user is trialing, false otherwise
     */
    public boolean isTrialing() {
        return "trialing".equalsIgnoreCase(subscriptionStatus);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id + '\'' +
                ", name='" + name + '\'' + '\'' +
                ", surname='" + surname + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", baseEntity=" + super.toString() + '\'' +
                ", externalId='" + externalId + '\'' +
                ", subscriptionPlan='" + subscriptionPlan + '\'' +
                '}';
    }
}

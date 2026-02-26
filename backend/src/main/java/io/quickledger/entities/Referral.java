package io.quickledger.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "referrals")
public class Referral extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id", nullable = false)
    private User referrer;

    @Column(name = "referee_email", nullable = false, length = 255)
    private String refereeEmail;

    @Column(name = "referee_name", length = 255)
    private String refereeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ReferralStatus status = ReferralStatus.PENDING;

    @Column(name = "referral_code", unique = true, length = 50)
    private String referralCode;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getReferrer() {
        return referrer;
    }

    public void setReferrer(User referrer) {
        this.referrer = referrer;
    }

    public String getRefereeEmail() {
        return refereeEmail;
    }

    public void setRefereeEmail(String refereeEmail) {
        this.refereeEmail = refereeEmail;
    }

    public String getRefereeName() {
        return refereeName;
    }

    public void setRefereeName(String refereeName) {
        this.refereeName = refereeName;
    }

    public ReferralStatus getStatus() {
        return status;
    }

    public void setStatus(ReferralStatus status) {
        this.status = status;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    @Override
    public String toString() {
        return "Referral{" +
                "id=" + id +
                ", refereeEmail='" + refereeEmail + '\'' +
                ", refereeName='" + refereeName + '\'' +
                ", status=" + status +
                ", referralCode='" + referralCode + '\'' +
                '}';
    }
}

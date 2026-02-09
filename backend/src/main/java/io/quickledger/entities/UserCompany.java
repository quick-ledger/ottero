package io.quickledger.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "user_companies")
public class UserCompany{
    @EmbeddedId
    private UserCompanyId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("companyId")
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "role")
    private String role; // can be either "owner" or "employee"

    //@Column(name = "is_default", columnDefinition = "boolean default false")
    //private boolean defaultCompany;

    // getters and setters...

    public UserCompanyId getId() {
        return id;
    }

    public void setId(UserCompanyId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserCompany{" +
                "id=" + id + '\'' +
                ", userId=" + user.getId() +  '\'' +
                ", companyId=" + company.getId() +  '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
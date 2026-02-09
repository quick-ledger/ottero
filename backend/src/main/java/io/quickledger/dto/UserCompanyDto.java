package io.quickledger.dto;

public class UserCompanyDto {
    private Long userId;
    private Long companyId;
    private String role;
    private boolean defaultCompany;

    // getters and setters...

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isDefaultCompany() {
        return defaultCompany;
    }

    public void setDefaultCompany(boolean defaultCompany) {
        this.defaultCompany = defaultCompany;
    }
}
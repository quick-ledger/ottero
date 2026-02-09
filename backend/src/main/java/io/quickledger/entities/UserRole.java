package io.quickledger.entities;

public enum UserRole {
    ROLE_OWNER("owner"),
    ROLE_EMPLOYEE("employee"),
    ROLE_ADMIN("admin");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return this.role;
    }
}

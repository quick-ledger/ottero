package io.quickledger.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * MBH: This is for 1 time token for quote approval.
 * We need special form to handle it just FYI in react.js.
 */
@Entity
@Table(name = "temp_tokens", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "client_id", "company_id", "token_type" }) })
public class TempToken extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", length = 4000, nullable = false)
    private String token;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    public enum TokenType {
        QUOTE_TOKEN,
        INVOICE_TOKEN
    }

    // Constructors, getters, and setters

    public TempToken() {
    }

    public TempToken(String token, Long clientId, Long companyId, LocalDateTime expiryTime, TokenType tokenType) {
        this.token = token;
        this.clientId = clientId;
        this.companyId = companyId;
        this.expiryTime = expiryTime;
        this.tokenType = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String toString() {
        return "TempToken(id=" + this.getId() + ", token=" + this.getToken() + ", companyId=" + this.getCompanyId()
                + ", clientId=" + this.getClientId() + ", tokenType=" + this.getTokenType() + ", expiryTime="
                + this.getExpiryTime() + ")";
    }
}

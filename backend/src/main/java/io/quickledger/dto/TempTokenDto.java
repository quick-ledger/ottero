package io.quickledger.dto;

import io.quickledger.entities.TempToken.TokenType;
import java.time.LocalDateTime;

public class TempTokenDto {
    private Long id;
    private String token;
    private Long companyId;
    private Long clientId;
    private TokenType tokenType;
    private LocalDateTime expiryTime;

    // Constructors, getters, and setters

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

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    @Override
    public String toString() {
        return "TempTokenDTO{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", companyId=" + companyId +
                ", clientId=" + clientId +
                ", tokenType=" + tokenType +
                ", expiryTime=" + expiryTime +
                '}';
    }
}
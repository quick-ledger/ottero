package io.quickledger.dto.quote;

public class QuoteTempTokenValidationRequestDto {
    private String token;

    public QuoteTempTokenValidationRequestDto() {
    }

    public QuoteTempTokenValidationRequestDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

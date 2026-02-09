package io.quickledger.dto.quote;

public class QuoteTempTokenValidationResponseDto {
    private Long companyId;
    private Long clientId;
    private Long quoteId;

    public QuoteTempTokenValidationResponseDto() {
    }

    public QuoteTempTokenValidationResponseDto(Long companyId, Long clientId, Long quoteId) {
        this.companyId = companyId;
        this.clientId = clientId;
        this.quoteId = quoteId;
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

    public Long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(Long quoteId) {
        this.quoteId = quoteId;
    }
}
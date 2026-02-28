package io.quickledger.dto.expense;

import io.quickledger.entities.expense.ExpenseCategory;
import io.quickledger.entities.expense.ExpenseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ExpenseDto {

    private Long id;
    private Long companyId;
    private LocalDate expenseDate;
    private BigDecimal amount;
    private BigDecimal gstAmount;
    private BigDecimal netAmount;
    private ExpenseCategory category;
    private String vendor;
    private String expenseDescription;
    private ExpenseStatus status;
    private Boolean taxDeductible;
    private Boolean gstClaimable;
    private String referenceNumber;
    private String paymentMethod;
    private String notes;
    private String financialYear;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private List<ExpenseAttachmentDto> attachments;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(BigDecimal gstAmount) {
        this.gstAmount = gstAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getExpenseDescription() {
        return expenseDescription;
    }

    public void setExpenseDescription(String expenseDescription) {
        this.expenseDescription = expenseDescription;
    }

    public ExpenseStatus getStatus() {
        return status;
    }

    public void setStatus(ExpenseStatus status) {
        this.status = status;
    }

    public Boolean getTaxDeductible() {
        return taxDeductible;
    }

    public void setTaxDeductible(Boolean taxDeductible) {
        this.taxDeductible = taxDeductible;
    }

    public Boolean getGstClaimable() {
        return gstClaimable;
    }

    public void setGstClaimable(Boolean gstClaimable) {
        this.gstClaimable = gstClaimable;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(String financialYear) {
        this.financialYear = financialYear;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public List<ExpenseAttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<ExpenseAttachmentDto> attachments) {
        this.attachments = attachments;
    }
}

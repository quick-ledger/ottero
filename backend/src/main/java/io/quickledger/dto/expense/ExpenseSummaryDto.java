package io.quickledger.dto.expense;

import java.math.BigDecimal;
import java.util.List;

public class ExpenseSummaryDto {

    private BigDecimal totalExpenses;
    private BigDecimal totalGstClaimable;
    private BigDecimal totalTaxDeductible;
    private int expenseCount;
    private List<CategorySummary> byCategory;
    private List<MonthlySummary> byMonth;

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getTotalGstClaimable() {
        return totalGstClaimable;
    }

    public void setTotalGstClaimable(BigDecimal totalGstClaimable) {
        this.totalGstClaimable = totalGstClaimable;
    }

    public BigDecimal getTotalTaxDeductible() {
        return totalTaxDeductible;
    }

    public void setTotalTaxDeductible(BigDecimal totalTaxDeductible) {
        this.totalTaxDeductible = totalTaxDeductible;
    }

    public int getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(int expenseCount) {
        this.expenseCount = expenseCount;
    }

    public List<CategorySummary> getByCategory() {
        return byCategory;
    }

    public void setByCategory(List<CategorySummary> byCategory) {
        this.byCategory = byCategory;
    }

    public List<MonthlySummary> getByMonth() {
        return byMonth;
    }

    public void setByMonth(List<MonthlySummary> byMonth) {
        this.byMonth = byMonth;
    }

    public static class CategorySummary {
        private String category;
        private String categoryDisplayName;
        private BigDecimal amount;
        private BigDecimal gstAmount;
        private int count;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getCategoryDisplayName() {
            return categoryDisplayName;
        }

        public void setCategoryDisplayName(String categoryDisplayName) {
            this.categoryDisplayName = categoryDisplayName;
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

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class MonthlySummary {
        private String month;
        private BigDecimal amount;
        private BigDecimal gstAmount;
        private int count;

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
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

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}

package io.quickledger.dto;

import java.math.BigDecimal;
import java.util.List;

public class StatsDto {

    // Monthly Revenue Response
    public static class MonthlyRevenue {
        private String month;           // "2026-01"
        private BigDecimal revenue;     // Total revenue for the month
        private int invoiceCount;       // Number of invoices

        public MonthlyRevenue() {}

        public MonthlyRevenue(String month, BigDecimal revenue, int invoiceCount) {
            this.month = month;
            this.revenue = revenue;
            this.invoiceCount = invoiceCount;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
        public int getInvoiceCount() { return invoiceCount; }
        public void setInvoiceCount(int invoiceCount) { this.invoiceCount = invoiceCount; }
    }

    // Outstanding Invoices Response
    public static class OutstandingInvoices {
        private int count;              // Number of unpaid invoices
        private BigDecimal totalAmount; // Total amount outstanding

        public OutstandingInvoices() {}

        public OutstandingInvoices(int count, BigDecimal totalAmount) {
            this.count = count;
            this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }

    // Top Customer Response
    public static class TopCustomer {
        private Long customerId;
        private String customerName;
        private BigDecimal totalRevenue;
        private int invoiceCount;

        public TopCustomer() {}

        public TopCustomer(Long customerId, String customerName, BigDecimal totalRevenue, int invoiceCount) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.totalRevenue = totalRevenue;
            this.invoiceCount = invoiceCount;
        }

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        public int getInvoiceCount() { return invoiceCount; }
        public void setInvoiceCount(int invoiceCount) { this.invoiceCount = invoiceCount; }
    }
}

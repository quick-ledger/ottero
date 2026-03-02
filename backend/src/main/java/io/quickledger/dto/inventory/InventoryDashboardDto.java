package io.quickledger.dto.inventory;

import java.math.BigDecimal;
import java.util.List;

public class InventoryDashboardDto {

    private int totalProducts;
    private int lowStockCount;
    private int outOfStockCount;
    private BigDecimal totalInventoryValue;
    private List<LowStockAlertDto> lowStockAlerts;

    // Getters and Setters

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public int getOutOfStockCount() {
        return outOfStockCount;
    }

    public void setOutOfStockCount(int outOfStockCount) {
        this.outOfStockCount = outOfStockCount;
    }

    public BigDecimal getTotalInventoryValue() {
        return totalInventoryValue;
    }

    public void setTotalInventoryValue(BigDecimal totalInventoryValue) {
        this.totalInventoryValue = totalInventoryValue;
    }

    public List<LowStockAlertDto> getLowStockAlerts() {
        return lowStockAlerts;
    }

    public void setLowStockAlerts(List<LowStockAlertDto> lowStockAlerts) {
        this.lowStockAlerts = lowStockAlerts;
    }
}

package io.quickledger.controllers;

import io.quickledger.dto.inventory.InventoryDashboardDto;
import io.quickledger.dto.inventory.LowStockAlertDto;
import io.quickledger.dto.inventory.StockMovementDto;
import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/inventory")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<InventoryDashboardDto> getDashboard(
            @PathVariable Long companyId,
            @UserIdAuth final User user) {
        InventoryDashboardDto dashboard = inventoryService.getInventoryDashboard(companyId, user);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockAlertDto>> getLowStockAlerts(
            @PathVariable Long companyId,
            @UserIdAuth final User user) {
        List<LowStockAlertDto> alerts = inventoryService.getLowStockAlerts(companyId, user);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/movements")
    public ResponseEntity<Page<StockMovementDto>> getAllMovements(
            @PathVariable Long companyId,
            Pageable pageable,
            @UserIdAuth final User user) {
        Page<StockMovementDto> movements = inventoryService.getAllMovements(companyId, pageable, user);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/products/{productId}/movements")
    public ResponseEntity<Page<StockMovementDto>> getProductMovements(
            @PathVariable Long companyId,
            @PathVariable Long productId,
            Pageable pageable,
            @UserIdAuth final User user) {
        Page<StockMovementDto> movements = inventoryService.getProductMovements(productId, companyId, pageable, user);
        return ResponseEntity.ok(movements);
    }

    @PostMapping("/products/{productId}/adjust")
    public ResponseEntity<StockMovementDto> adjustStock(
            @PathVariable Long companyId,
            @PathVariable Long productId,
            @RequestBody StockAdjustmentRequest request,
            @UserIdAuth final User user) {
        StockMovementDto movement = inventoryService.adjustStock(
                productId, request.getNewQuantity(), request.getReason(), companyId, user);
        return ResponseEntity.ok(movement);
    }

    public static class StockAdjustmentRequest {
        private int newQuantity;
        private String reason;

        public int getNewQuantity() {
            return newQuantity;
        }

        public void setNewQuantity(int newQuantity) {
            this.newQuantity = newQuantity;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}

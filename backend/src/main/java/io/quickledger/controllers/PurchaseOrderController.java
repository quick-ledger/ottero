package io.quickledger.controllers;

import io.quickledger.dto.purchaseorder.PurchaseOrderDto;
import io.quickledger.dto.purchaseorder.ReceiveItemsDto;
import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.PurchaseOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies/{companyId}/purchase-orders")
public class PurchaseOrderController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderController.class);
    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderDto> createPurchaseOrder(
            @PathVariable Long companyId,
            @RequestBody PurchaseOrderDto dto,
            @UserIdAuth final User user) {
        dto.setId(null); // Ensure new creation
        PurchaseOrderDto created = purchaseOrderService.createOrUpdate(dto, companyId, user);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseOrderDto>> getAllPurchaseOrders(
            @PathVariable Long companyId,
            Pageable pageable,
            @UserIdAuth final User user) {
        Page<PurchaseOrderDto> purchaseOrders = purchaseOrderService.getAllPurchaseOrders(companyId, pageable, user);
        return ResponseEntity.ok(purchaseOrders);
    }

    @GetMapping("/{poId}")
    public ResponseEntity<PurchaseOrderDto> getPurchaseOrderById(
            @PathVariable Long companyId,
            @PathVariable Long poId,
            @UserIdAuth final User user) {
        PurchaseOrderDto purchaseOrder = purchaseOrderService.getPurchaseOrderById(poId, companyId, user);
        return ResponseEntity.ok(purchaseOrder);
    }

    @PutMapping("/{poId}")
    public ResponseEntity<PurchaseOrderDto> updatePurchaseOrder(
            @PathVariable Long companyId,
            @PathVariable Long poId,
            @RequestBody PurchaseOrderDto dto,
            @UserIdAuth final User user) {
        dto.setId(poId);
        PurchaseOrderDto updated = purchaseOrderService.createOrUpdate(dto, companyId, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{poId}")
    public ResponseEntity<Void> deletePurchaseOrder(
            @PathVariable Long companyId,
            @PathVariable Long poId,
            @UserIdAuth final User user) {
        purchaseOrderService.deletePurchaseOrder(poId, companyId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{poId}/send")
    public ResponseEntity<PurchaseOrderDto> sendPurchaseOrder(
            @PathVariable Long companyId,
            @PathVariable Long poId,
            @UserIdAuth final User user) {
        PurchaseOrderDto sent = purchaseOrderService.sendPurchaseOrder(poId, companyId, user);
        return ResponseEntity.ok(sent);
    }

    @PostMapping("/{poId}/receive")
    public ResponseEntity<PurchaseOrderDto> receiveItems(
            @PathVariable Long companyId,
            @PathVariable Long poId,
            @RequestBody ReceiveItemsDto dto,
            @UserIdAuth final User user) {
        dto.setPurchaseOrderId(poId);
        PurchaseOrderDto updated = purchaseOrderService.receiveItems(dto, companyId, user);
        return ResponseEntity.ok(updated);
    }
}

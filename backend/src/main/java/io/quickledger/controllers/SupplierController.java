package io.quickledger.controllers;

import io.quickledger.dto.supplier.SupplierDto;
import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/suppliers")
public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    public ResponseEntity<SupplierDto> createSupplier(
            @PathVariable Long companyId,
            @RequestBody SupplierDto dto,
            @UserIdAuth final User user) {
        dto.setCompanyId(companyId);
        SupplierDto created = supplierService.createOrUpdate(dto, companyId, user);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<Page<SupplierDto>> getAllSuppliers(
            @PathVariable Long companyId,
            Pageable pageable,
            @UserIdAuth final User user) {
        Page<SupplierDto> suppliers = supplierService.getAllSuppliers(companyId, pageable, user);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SupplierDto>> getActiveSuppliers(
            @PathVariable Long companyId,
            @UserIdAuth final User user) {
        List<SupplierDto> suppliers = supplierService.getActiveSuppliers(companyId, user);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SupplierDto>> searchSuppliers(
            @PathVariable Long companyId,
            @RequestParam String searchTerm,
            Pageable pageable,
            @UserIdAuth final User user) {
        Page<SupplierDto> suppliers = supplierService.searchSuppliers(companyId, searchTerm, pageable, user);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDto> getSupplierById(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @UserIdAuth final User user) {
        SupplierDto supplier = supplierService.getSupplierById(id, companyId, user);
        return ResponseEntity.ok(supplier);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDto> updateSupplier(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @RequestBody SupplierDto dto,
            @UserIdAuth final User user) {
        dto.setId(id);
        dto.setCompanyId(companyId);
        SupplierDto updated = supplierService.createOrUpdate(dto, companyId, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @UserIdAuth final User user) {
        supplierService.deleteSupplier(id, companyId, user);
        return ResponseEntity.noContent().build();
    }
}

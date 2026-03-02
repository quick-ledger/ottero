package io.quickledger.controllers;

import io.quickledger.dto.product.ProductItemAttributeDefinitionDto;
import io.quickledger.dto.product.ProductItemAttributeValueDto;
import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.ProductAttributeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}")
public class ProductAttributeController {

    private static final Logger logger = LoggerFactory.getLogger(ProductAttributeController.class);
    private final ProductAttributeService productAttributeService;

    public ProductAttributeController(ProductAttributeService productAttributeService) {
        this.productAttributeService = productAttributeService;
    }

    // ========== Attribute Definitions ==========

    @PostMapping("/product-attribute-definitions")
    public ResponseEntity<ProductItemAttributeDefinitionDto> createDefinition(
            @PathVariable Long companyId,
            @RequestBody ProductItemAttributeDefinitionDto dto,
            @UserIdAuth final User user) {
        dto.setCompanyId(companyId);
        ProductItemAttributeDefinitionDto created = productAttributeService.createOrUpdateDefinition(dto, companyId, user);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/product-attribute-definitions")
    public ResponseEntity<List<ProductItemAttributeDefinitionDto>> getAllDefinitions(
            @PathVariable Long companyId,
            @UserIdAuth final User user) {
        List<ProductItemAttributeDefinitionDto> definitions = productAttributeService.getAllDefinitions(companyId, user);
        return ResponseEntity.ok(definitions);
    }

    @GetMapping("/product-attribute-definitions/{id}")
    public ResponseEntity<ProductItemAttributeDefinitionDto> getDefinitionById(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @UserIdAuth final User user) {
        ProductItemAttributeDefinitionDto definition = productAttributeService.getDefinitionById(id, companyId, user);
        return ResponseEntity.ok(definition);
    }

    @PutMapping("/product-attribute-definitions/{id}")
    public ResponseEntity<ProductItemAttributeDefinitionDto> updateDefinition(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @RequestBody ProductItemAttributeDefinitionDto dto,
            @UserIdAuth final User user) {
        dto.setId(id);
        dto.setCompanyId(companyId);
        ProductItemAttributeDefinitionDto updated = productAttributeService.createOrUpdateDefinition(dto, companyId, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/product-attribute-definitions/{id}")
    public ResponseEntity<Void> deleteDefinition(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @UserIdAuth final User user) {
        productAttributeService.deleteDefinition(id, companyId, user);
        return ResponseEntity.noContent().build();
    }

    // ========== Product Attribute Values ==========

    @GetMapping("/products/{productId}/attributes")
    public ResponseEntity<List<ProductItemAttributeValueDto>> getProductAttributes(
            @PathVariable Long companyId,
            @PathVariable Long productId,
            @UserIdAuth final User user) {
        List<ProductItemAttributeValueDto> attributes = productAttributeService.getProductAttributes(productId, companyId, user);
        return ResponseEntity.ok(attributes);
    }

    @PutMapping("/products/{productId}/attributes")
    public ResponseEntity<List<ProductItemAttributeValueDto>> setProductAttributes(
            @PathVariable Long companyId,
            @PathVariable Long productId,
            @RequestBody List<ProductItemAttributeValueDto> values,
            @UserIdAuth final User user) {
        List<ProductItemAttributeValueDto> saved = productAttributeService.setProductAttributes(productId, values, companyId, user);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/products/{productId}/attributes/{definitionId}")
    public ResponseEntity<Void> deleteProductAttribute(
            @PathVariable Long companyId,
            @PathVariable Long productId,
            @PathVariable Long definitionId,
            @UserIdAuth final User user) {
        productAttributeService.deleteProductAttribute(productId, definitionId, companyId, user);
        return ResponseEntity.noContent().build();
    }
}

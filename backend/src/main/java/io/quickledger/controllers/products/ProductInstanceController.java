package io.quickledger.controllers.products;

import io.quickledger.dto.product.ProductInstanceDto;
import io.quickledger.services.product.ProductInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/companies/{companyId}/product-definition/{productDefinitionId}/product-instances")
public class ProductInstanceController {

    private static final Logger logger = LoggerFactory.getLogger(ProductInstanceController.class);
    private final ProductInstanceService productInstanceService;

    public ProductInstanceController(ProductInstanceService productInstanceService) {
        this.productInstanceService = productInstanceService;
    }

    @PostMapping
    public ResponseEntity<ProductInstanceDto> createProductInstance(@PathVariable Long companyId, @PathVariable Long productDefinitionId, @RequestBody ProductInstanceDto productInstanceDto) {
        logger.info("Creating product instance for company {} and product definition {}", companyId, productDefinitionId, productInstanceDto.toString());
        ProductInstanceDto createdProductInstance = productInstanceService.save(companyId, productDefinitionId, productInstanceDto);
        return new ResponseEntity<>(createdProductInstance, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductInstanceDto> getProductInstance(@PathVariable Long companyId, @PathVariable Long productDefinitionId , @PathVariable Long id) {
        Optional<ProductInstanceDto> productInstanceDto = productInstanceService.findProductInstanceById(companyId, productDefinitionId, id);
        if (productInstanceDto.isPresent()) {
            return ResponseEntity.ok(productInstanceDto.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductInstanceDto> updateProductInstance(@PathVariable Long companyId, @PathVariable Long productDefinitionId, @PathVariable Long id, @RequestBody ProductInstanceDto productInstanceDto) {
        if (!id.equals(productInstanceDto.getId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ProductInstanceDto updatedProductInstance = productInstanceService.updateProductInstance(companyId, productInstanceDto);
        return ResponseEntity.ok(updatedProductInstance);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductInstance(@PathVariable Long companyId, @PathVariable Long productDefinitionId, @PathVariable Long id) {
        productInstanceService.deleteProductInstanceById(companyId, id);
        return ResponseEntity.noContent().build();
    }
}
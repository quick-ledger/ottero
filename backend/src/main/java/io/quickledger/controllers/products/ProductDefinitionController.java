package io.quickledger.controllers.products;

import io.quickledger.dto.product.ProductDefinitionDto;
import io.quickledger.dto.product.ProductDefinitionTabularDto;
import io.quickledger.services.product.ProductDefinitionService;
import io.quickledger.services.product.JsonSchemaGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/companies/{companyId}/product-definition")
public class ProductDefinitionController {

    private final ProductDefinitionService productDefinitionService;
    private final JsonSchemaGenerator jsonSchemaGenerator;

    public ProductDefinitionController(ProductDefinitionService productDefinitionService, JsonSchemaGenerator jsonSchemaGenerator) {
        this.productDefinitionService = productDefinitionService;
        this.jsonSchemaGenerator = jsonSchemaGenerator;
    }

    /*@PostMapping
    public ResponseEntity<ProductDefinitionDto> createProductDefinition(@PathVariable Long companyId, @RequestBody ProductDefinitionDto productDefinitionDto) {
        return ResponseEntity.ok(productDefinitionService.save(productDefinitionDto));
    }*/

    @PostMapping
    public ResponseEntity<ProductDefinitionTabularDto> createProductDefinition(@PathVariable Long companyId, @RequestBody ProductDefinitionTabularDto inputDto) {
        return ResponseEntity.ok(productDefinitionService.save(inputDto, companyId));
    }

    /**
     * This is used to populate the asset creation form  using rjsf/core in react.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDefinitionDto> getProductDefinition(@PathVariable Long companyId, @PathVariable Long id) {
        return productDefinitionService.findProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * This is used to populate the asset definition form
     */
    @GetMapping("/{id}/tabular")
    public ResponseEntity<ProductDefinitionTabularDto> getProductDefinitionTabular(@PathVariable Long companyId, @PathVariable Long id) {
        //TODO check if the authenticated user has access to this product definition. RG
        Optional<ProductDefinitionDto> dto = productDefinitionService.findProductById(id);
        if (dto.isPresent()) {
            return ResponseEntity.ok(jsonSchemaGenerator.convertJsonSchemaToTabular(dto.get()));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * This is used to populate the asset definition list with json schema
     */
    @GetMapping
    public ResponseEntity<List<ProductDefinitionDto>> getAllProductDefinitions(@PathVariable Long companyId) {
        return ResponseEntity.ok(productDefinitionService.findAllProducts(companyId));
    }

    /**
     * This is used to populate the asset definition list tabular
     */
    @GetMapping("/tabular")
    public ResponseEntity<List<ProductDefinitionTabularDto>> getAllProductDefinitionsTabular(@PathVariable Long companyId) {

        List<ProductDefinitionTabularDto> output = new ArrayList<>();
        List <ProductDefinitionDto> list = productDefinitionService.findAllProducts(companyId);
        //how to add to output?
        list.forEach(productDefinitionDto -> {
            output.add(jsonSchemaGenerator.convertJsonSchemaToTabular(productDefinitionDto));
        });
        return ResponseEntity.ok(output);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDefinitionDto> updateProductDefinition(@PathVariable Long companyId, @PathVariable Long id, @RequestBody ProductDefinitionDto productDefinitionDto) {
        return ResponseEntity.ok(productDefinitionService.updateProduct(productDefinitionDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductDefinition(@PathVariable Long companyId, @PathVariable Long id) {
        //TODO make sure the id belongs to the company. RG
        productDefinitionService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }
}
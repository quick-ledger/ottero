package io.quickledger.controllers.products;

import io.quickledger.dto.product.ProductItemDto;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.product.ProductItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/products")
public class ProductItemController {

    private final ProductItemService productItemService;
    private static final Logger logger = LoggerFactory.getLogger(ProductItemController.class);

    public ProductItemController(ProductItemService productItemService) {
        this.productItemService = productItemService;
    }

    @GetMapping
    public ResponseEntity<List<ProductItemDto>> getProducts(@PathVariable Long companyId) {
        List<ProductItemDto> products = productItemService.findAllProducts(companyId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductItemDto> getProducts(@PathVariable Long companyId, @PathVariable Long id) {
        ProductItemDto product = productItemService.findById(id);
        return ResponseEntity.ok(product);
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ProductItemDto> saveProduct(@PathVariable Long companyId, @RequestBody ProductItemDto productItemDto) {
        ProductItemDto productItemDto1 = productItemService.save(productItemDto, companyId);
        return ResponseEntity.ok(productItemDto1);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long companyId, @PathVariable Long id, UserIdAuth userIdAuth) {
        //TODO check if the user is the owner
        productItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package io.quickledger.services.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quickledger.dto.product.ProductInstanceDto;
import io.quickledger.entities.product.ProductDefinition;
import io.quickledger.entities.product.ProductInstance;
import io.quickledger.mappers.product.ProductInstanceMapper;
import io.quickledger.repositories.product.ProductDefinitionRepository;
import io.quickledger.repositories.product.ProductInstanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Transactional
public class ProductInstanceService {

    private final ProductInstanceRepository productInstanceRepository;
    private final ProductInstanceMapper productInstanceMapper;
    private final ProductDefinitionRepository productDefinitionRepository;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final ObjectMapper objectMapper;

    public ProductInstanceService(ProductInstanceRepository productInstanceRepository, ProductInstanceMapper productInstanceMapper, ProductDefinitionRepository productDefinitionRepository, JsonSchemaValidator jsonSchemaValidator, ObjectMapper objectMapper) {
        this.productInstanceRepository = productInstanceRepository;
        this.productInstanceMapper = productInstanceMapper;
        this.productDefinitionRepository = productDefinitionRepository;
        this.jsonSchemaValidator = jsonSchemaValidator;
        this.objectMapper = objectMapper;
    }

    protected void validateJsonSchema(String jsonSchema, String jsonPayload) {
        // Add logic to validate the jsonPayload against the jsonSchema

    }

    public ProductInstanceDto save(Long companyId, Long productDefinitionId, ProductInstanceDto productInstanceDto) {
        // Add logic to handle companyId
        // INFO: don't really need company Id as ProductInstance is belong to Product Definition that is belong to a Company but for indexing we can add it in future.
        productInstanceDto.setProductDefinitionId(productDefinitionId);
        ProductDefinition productDefinition = productDefinitionRepository.findById(productDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product Definition not found with id " + productDefinitionId));
        // use validateJsonSchema() method from JsonSchemaValidator class to validate the productInstanceDto against the productDefinition schema
        String productInstanceJson;
        String productDefinitionSchema;
        try {
            productInstanceJson = objectMapper.writeValueAsString(productInstanceDto.getInstanceAttributes());
            productDefinitionSchema = objectMapper.writeValueAsString(productDefinition.getProductAttributes().get("schema"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting object to JSON string", e);
        }

        // Validate productInstanceDto against productDefinition schema
        JsonSchemaValidator.ValidationResult validationResult = jsonSchemaValidator.validateJsonSchema(productDefinitionSchema, productInstanceJson);
        if (!validationResult.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationResult.getMessage());
        }

        ProductInstance productInstance = productInstanceMapper.toEntity(productInstanceDto);
        productInstance = productInstanceRepository.save(productInstance);
        return productInstanceMapper.toDto(productInstance);
    }

    @Transactional(readOnly = true)
    public Optional<ProductInstanceDto> findProductInstanceById(Long companyId,Long productDefinitionId, Long id) {
        // Add logic to handle companyId
        return productInstanceRepository.findById(id).map(productInstanceMapper::toDto);
    }

    public ProductInstanceDto updateProductInstance(Long companyId, ProductInstanceDto productInstanceDto) {
        // Add logic to handle companyId
        ProductInstance productInstance = productInstanceMapper.toEntity(productInstanceDto);
        productInstance = productInstanceRepository.save(productInstance);
        return productInstanceMapper.toDto(productInstance);
    }

    public void deleteProductInstanceById(Long companyId, Long id) {
        // Add logic to handle companyId
        if (!productInstanceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Instance not found with id " + id);
        }
        productInstanceRepository.deleteById(id);
    }
}
package io.quickledger.services.product;

import com.fasterxml.jackson.databind.JsonNode;
import io.quickledger.dto.product.ProductDefinitionDto;
import io.quickledger.dto.product.ProductDefinitionTabularDto;
import io.quickledger.entities.Company;
import io.quickledger.entities.product.ProductDefinition;
import io.quickledger.mappers.product.ProductDefinitionMapper;
import io.quickledger.repositories.CompanyRepository;
import io.quickledger.repositories.product.ProductDefinitionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ProductDefinitionService {

    private final ProductDefinitionRepository productDefinitionRepository;
    private final ProductDefinitionMapper productDefinitionMapper;
    private final CompanyRepository companyRepository;
    private final JsonSchemaGenerator jsonSchemaGenerator;

    public ProductDefinitionService(ProductDefinitionRepository productDefinitionRepository, ProductDefinitionMapper productDefinitionMapper, CompanyRepository companyRepository, JsonSchemaGenerator jsonSchemaGenerator) {
        this.productDefinitionRepository = productDefinitionRepository;
        this.productDefinitionMapper = productDefinitionMapper;
        this.companyRepository = companyRepository;
        this.jsonSchemaGenerator = jsonSchemaGenerator;
    }

    public ProductDefinitionTabularDto save(ProductDefinitionTabularDto inputDto, Long companyId) {

        JsonNode productSchema = jsonSchemaGenerator.generateJsonSchema(inputDto);

        //TODO: we do not need ProductDefinitionDto to do the mapping now. map inputDto and productSchema directly to ProductDefinition entity. RG
        ProductDefinitionDto productDefinitionDto = new ProductDefinitionDto();
        productDefinitionDto.setName(productSchema.get("title").asText());
        productDefinitionDto.setProductDescription(productSchema.get("description").asText());
        productDefinitionDto.setProductAttributes(List.of(Map.of("schema", productSchema)));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Company not found with id " + companyId)); // This should be considered as bad request
        ProductDefinition productDefinition = productDefinitionMapper.toEntity(productDefinitionDto);
        productDefinition.setCompany(company);
        productDefinition = productDefinitionRepository.save(productDefinition);

        inputDto.setId(productDefinition.getId());
        return inputDto;
    }

    @Transactional(readOnly = true)
    public Optional<ProductDefinitionDto> findProductById(Long id) {
        return productDefinitionRepository.findById(id).map(productDefinitionMapper::toDto);
    }

    public ProductDefinitionDto updateProduct(ProductDefinitionDto productDefinitionDTO) {
        ProductDefinition productDefinition = productDefinitionMapper.toEntity(productDefinitionDTO);
        productDefinition = productDefinitionRepository.save(productDefinition);
        return productDefinitionMapper.toDto(productDefinition);
    }

    public List<ProductDefinitionDto> findAllProducts(Long companyId) {
        return productDefinitionMapper.toDtoList(productDefinitionRepository.findAllByCompanyId(companyId)) ;
    }

    public List<ProductDefinitionDto> findProductByName(String name) {
        return productDefinitionMapper.toDtoList(productDefinitionRepository.findByName(name));
    }

    public List<ProductDefinitionDto> findProductByAttribute(String attributeName, String attributeValue) {
        return productDefinitionMapper.toDtoList(productDefinitionRepository.findByAttribute(attributeName, attributeValue));
    }

    public void deleteProductById(Long id) {
        //TODO this should only delete if there is no product associated with this product definition. RG
        productDefinitionRepository.deleteById(id);
    }
}
package io.quickledger.services;

import io.quickledger.dto.product.ProductItemAttributeDefinitionDto;
import io.quickledger.dto.product.ProductItemAttributeValueDto;
import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.entities.product.ProductItem;
import io.quickledger.entities.product.ProductItemAttributeDefinition;
import io.quickledger.entities.product.ProductItemAttributeValue;
import io.quickledger.mappers.product.ProductItemAttributeDefinitionMapper;
import io.quickledger.mappers.product.ProductItemAttributeValueMapper;
import io.quickledger.repositories.product.ProductItemAttributeDefinitionRepository;
import io.quickledger.repositories.product.ProductItemAttributeValueRepository;
import io.quickledger.repositories.product.ProductItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductAttributeService {

    private static final Logger logger = LoggerFactory.getLogger(ProductAttributeService.class);

    private final ProductItemAttributeDefinitionRepository definitionRepository;
    private final ProductItemAttributeValueRepository valueRepository;
    private final ProductItemRepository productItemRepository;
    private final ProductItemAttributeDefinitionMapper definitionMapper;
    private final ProductItemAttributeValueMapper valueMapper;
    private final PlanService planService;

    public ProductAttributeService(
            ProductItemAttributeDefinitionRepository definitionRepository,
            ProductItemAttributeValueRepository valueRepository,
            ProductItemRepository productItemRepository,
            ProductItemAttributeDefinitionMapper definitionMapper,
            ProductItemAttributeValueMapper valueMapper,
            PlanService planService) {
        this.definitionRepository = definitionRepository;
        this.valueRepository = valueRepository;
        this.productItemRepository = productItemRepository;
        this.definitionMapper = definitionMapper;
        this.valueMapper = valueMapper;
        this.planService = planService;
    }

    private void validateAccess(User user) {
        planService.requireFeature(user, PlanService.Feature.INVENTORY_MANAGEMENT);
    }

    // ========== Attribute Definitions ==========

    @Transactional
    public ProductItemAttributeDefinitionDto createOrUpdateDefinition(
            ProductItemAttributeDefinitionDto dto, Long companyId, User user) {
        validateAccess(user);

        ProductItemAttributeDefinition definition;

        if (dto.getId() != null) {
            definition = definitionRepository.findByIdAndCompanyId(dto.getId(), companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Attribute definition not found"));
            definitionMapper.updateEntityFromDto(dto, definition);
        } else {
            // Check for duplicate name
            if (definitionRepository.existsByNameAndCompanyId(dto.getName(), companyId)) {
                throw new IllegalArgumentException("Attribute definition with name '" + dto.getName() + "' already exists");
            }
            definition = definitionMapper.toEntity(dto);
            definition.setCompany(new Company(companyId));
        }

        definition = definitionRepository.save(definition);
        return definitionMapper.toDto(definition);
    }

    @Transactional(readOnly = true)
    public List<ProductItemAttributeDefinitionDto> getAllDefinitions(Long companyId, User user) {
        validateAccess(user);
        return definitionRepository.findByCompanyIdOrderByNameAsc(companyId)
                .stream()
                .map(definitionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductItemAttributeDefinitionDto getDefinitionById(Long id, Long companyId, User user) {
        validateAccess(user);
        ProductItemAttributeDefinition definition = definitionRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute definition not found"));
        return definitionMapper.toDto(definition);
    }

    @Transactional
    public void deleteDefinition(Long id, Long companyId, User user) {
        validateAccess(user);
        ProductItemAttributeDefinition definition = definitionRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute definition not found"));

        // Delete all values using this definition first
        valueRepository.deleteByDefinitionId(id);

        definitionRepository.delete(definition);
        logger.info("Deleted attribute definition: {} for company: {}", id, companyId);
    }

    // ========== Attribute Values ==========

    @Transactional(readOnly = true)
    public List<ProductItemAttributeValueDto> getProductAttributes(Long productId, Long companyId, User user) {
        validateAccess(user);

        // Verify product belongs to company
        ProductItem product = productItemRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (!product.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Product does not belong to company");
        }

        return valueRepository.findByProductItemId(productId)
                .stream()
                .map(valueMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ProductItemAttributeValueDto> setProductAttributes(
            Long productId, List<ProductItemAttributeValueDto> values, Long companyId, User user) {
        validateAccess(user);

        ProductItem product = productItemRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (!product.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Product does not belong to company");
        }

        List<ProductItemAttributeValue> savedValues = new ArrayList<>();

        for (ProductItemAttributeValueDto dto : values) {
            ProductItemAttributeDefinition definition = definitionRepository
                    .findByIdAndCompanyId(dto.getDefinitionId(), companyId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Attribute definition not found: " + dto.getDefinitionId()));

            // Check if value already exists for this product + definition
            ProductItemAttributeValue value = valueRepository
                    .findByProductItemIdAndDefinitionId(productId, dto.getDefinitionId())
                    .orElse(null);

            if (value == null) {
                value = new ProductItemAttributeValue();
                value.setProductItem(product);
                value.setDefinition(definition);
            }

            value.setValue(dto.getValue());
            savedValues.add(valueRepository.save(value));
        }

        return savedValues.stream()
                .map(valueMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProductAttribute(Long productId, Long definitionId, Long companyId, User user) {
        validateAccess(user);

        ProductItemAttributeValue value = valueRepository
                .findByProductItemIdAndDefinitionId(productId, definitionId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute value not found"));

        // Verify product belongs to company
        if (!value.getProductItem().getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Product does not belong to company");
        }

        valueRepository.delete(value);
    }
}

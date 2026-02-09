package io.quickledger.services.product;

import io.quickledger.dto.product.ProductItemDto;
import io.quickledger.entities.*;
import io.quickledger.entities.product.ProductItem;
import io.quickledger.mappers.product.ProductItemMapper;
import io.quickledger.repositories.product.ProductItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductItemService {

    private static final Logger logger = LoggerFactory.getLogger(ProductItemService.class);

    private final ProductItemRepository productItemRepository;
    private final ProductItemMapper mapper;

    public ProductItemService(ProductItemRepository productItemRepository, ProductItemMapper mapper, ProductItemMapper mapper1) {
        this.productItemRepository = productItemRepository;
        this.mapper = mapper1;
    }


    public List<ProductItemDto> findAllProducts(Long companyId) {
        List<ProductItem> products = productItemRepository.findAllByCompanyId(companyId);

        products.stream().map(productItem -> {
            ProductItemDto productItemDto = mapper.toDto(productItem);
            return productItemDto;
        }).collect(Collectors.toList());

        return products.stream().map(
            mapper::toDto
        ).collect(Collectors.toList());


    }

    public ProductItemDto save(ProductItemDto productItemDto, Long companyId) {
        ProductItem productItem = mapper.toEntity(productItemDto);
        productItem.setCompany(new Company(companyId));
        ProductItem savedProductItem = productItemRepository.save(productItem);
        return mapper.toDto(savedProductItem);
    }

    public void delete(Long id) {
        productItemRepository.deleteById(id);
    }

    public ProductItemDto findById(Long id) {
        Optional<ProductItem> productItem = productItemRepository.findById(id);
        return productItem.map(mapper::toDto).orElse(null);
    }
}
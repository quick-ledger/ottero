package io.quickledger.repositories.product;

import io.quickledger.entities.product.ProductDefinition;

import java.util.List;

public interface ProductDefinitionRepositoryCustom {
    List<ProductDefinition> findByAttribute(String attributeName, String attributeValue);
}
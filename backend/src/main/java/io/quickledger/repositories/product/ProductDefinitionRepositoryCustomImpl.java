package io.quickledger.repositories.product;

import io.quickledger.entities.product.ProductDefinition;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;

public class ProductDefinitionRepositoryCustomImpl implements ProductDefinitionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ProductDefinition> findByAttribute(String attributeName, String attributeValue) {
        // INFO: This one is using JSONPath syntax but seems like the performance of using JSON_EXTRACT is better, MBH will test it in future
        /*Query query = entityManager.createNativeQuery("SELECT * FROM product_definition WHERE product_attributes -> :attributeName = :attributeValue", ProductDefinition.class);
        query.setParameter("attributeName", "$." + attributeName);
        query.setParameter("attributeValue", attributeValue);*/
        // INFO: This one is using JSON_EXTRACT function which is faster than JSONPath
        String sql = String.format("SELECT * FROM product_definition WHERE JSON_EXTRACT(product_attributes, '$.%s') = '\"%s\"'", attributeName, attributeValue);
        Query query = entityManager.createNativeQuery(sql, ProductDefinition.class);
        return query.getResultList();
    }
}
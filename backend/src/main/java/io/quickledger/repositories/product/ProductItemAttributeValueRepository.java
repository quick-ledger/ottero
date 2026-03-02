package io.quickledger.repositories.product;

import io.quickledger.entities.product.ProductItemAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductItemAttributeValueRepository extends JpaRepository<ProductItemAttributeValue, Long> {

    List<ProductItemAttributeValue> findByProductItemId(Long productItemId);

    Optional<ProductItemAttributeValue> findByProductItemIdAndDefinitionId(Long productItemId, Long definitionId);

    void deleteByProductItemId(Long productItemId);

    void deleteByDefinitionId(Long definitionId);
}

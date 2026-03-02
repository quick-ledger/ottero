package io.quickledger.repositories.product;

import io.quickledger.entities.product.ProductItemAttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductItemAttributeDefinitionRepository extends JpaRepository<ProductItemAttributeDefinition, Long> {

    List<ProductItemAttributeDefinition> findByCompanyIdOrderByNameAsc(Long companyId);

    Optional<ProductItemAttributeDefinition> findByIdAndCompanyId(Long id, Long companyId);

    Optional<ProductItemAttributeDefinition> findByNameAndCompanyId(String name, Long companyId);

    boolean existsByNameAndCompanyId(String name, Long companyId);
}

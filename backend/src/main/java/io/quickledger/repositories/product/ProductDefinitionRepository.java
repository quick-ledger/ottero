package io.quickledger.repositories.product;

import io.quickledger.entities.product.ProductDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDefinitionRepository extends JpaRepository<ProductDefinition, Long>, ProductDefinitionRepositoryCustom {
    List<ProductDefinition> findByName(String name);
    List<ProductDefinition> findAllByCompanyId(Long companyId);
}
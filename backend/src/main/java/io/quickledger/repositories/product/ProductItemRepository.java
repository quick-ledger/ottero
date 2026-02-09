package io.quickledger.repositories.product;

import io.quickledger.entities.product.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductItemRepository extends JpaRepository<ProductItem, Long> {
    List<ProductItem> findAllByCompanyId(Long companyId);
}
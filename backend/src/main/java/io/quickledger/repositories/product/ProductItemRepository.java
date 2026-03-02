package io.quickledger.repositories.product;

import io.quickledger.entities.product.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItem, Long> {

    List<ProductItem> findAllByCompanyId(Long companyId);

    Optional<ProductItem> findByIdAndCompanyId(Long id, Long companyId);

    // Inventory-related queries
    @Query("SELECT p FROM ProductItem p WHERE p.company.id = :companyId AND p.trackInventory = true")
    List<ProductItem> findTrackedProducts(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(p) FROM ProductItem p WHERE p.company.id = :companyId AND p.trackInventory = true")
    int countTrackedProducts(@Param("companyId") Long companyId);

    @Query("SELECT p FROM ProductItem p WHERE p.company.id = :companyId AND p.trackInventory = true " +
           "AND p.quantityOnHand <= p.reorderPoint AND p.reorderPoint IS NOT NULL")
    List<ProductItem> findLowStockProducts(@Param("companyId") Long companyId);

    @Query("SELECT p FROM ProductItem p WHERE p.company.id = :companyId AND p.trackInventory = true " +
           "AND p.quantityOnHand = 0")
    List<ProductItem> findOutOfStockProducts(@Param("companyId") Long companyId);

    @Query("SELECT COALESCE(SUM(p.quantityOnHand * p.price), 0) FROM ProductItem p " +
           "WHERE p.company.id = :companyId AND p.trackInventory = true")
    BigDecimal calculateTotalInventoryValue(@Param("companyId") Long companyId);
}
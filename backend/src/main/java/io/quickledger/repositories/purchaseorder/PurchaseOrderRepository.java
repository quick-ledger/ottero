package io.quickledger.repositories.purchaseorder;

import io.quickledger.entities.purchaseorder.PurchaseOrder;
import io.quickledger.entities.purchaseorder.PurchaseOrder.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Page<PurchaseOrder> findAllByCompanyIdOrderByCreatedDateDesc(Long companyId, Pageable pageable);

    Optional<PurchaseOrder> findByIdAndCompanyId(Long id, Long companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT po FROM PurchaseOrder po WHERE po.id = :id AND po.company.id = :companyId")
    Optional<PurchaseOrder> findByIdAndCompanyIdForUpdate(@Param("id") Long id, @Param("companyId") Long companyId);

    List<PurchaseOrder> findBySupplierId(Long supplierId);

    Page<PurchaseOrder> findByCompanyIdAndStatus(Long companyId, PurchaseOrderStatus status, Pageable pageable);

    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.company.id = :companyId " +
           "AND MONTH(po.createdDate) = MONTH(CURRENT_DATE) AND YEAR(po.createdDate) = YEAR(CURRENT_DATE)")
    long countMonthlyPurchaseOrdersByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.company.id = :companyId AND " +
           "(LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(po.supplier.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<PurchaseOrder> searchPurchaseOrders(
            @Param("companyId") Long companyId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
}

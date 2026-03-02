package io.quickledger.repositories.inventory;

import io.quickledger.entities.inventory.StockMovement;
import io.quickledger.entities.inventory.StockMovement.ReferenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findAllByCompanyIdOrderByCreatedDateDesc(Long companyId, Pageable pageable);

    Page<StockMovement> findAllByProductItemIdOrderByCreatedDateDesc(Long productItemId, Pageable pageable);

    List<StockMovement> findByReferenceTypeAndReferenceId(ReferenceType referenceType, Long referenceId);

    Page<StockMovement> findByCompanyIdAndProductItemIdOrderByCreatedDateDesc(
            Long companyId, Long productItemId, Pageable pageable);
}

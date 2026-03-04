package io.quickledger.repositories.serviceitem;

import io.quickledger.entities.serviceitem.ServiceItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
    List<ServiceItem> findByCompanyId(Long companyId);

    Page<ServiceItem> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT s FROM ServiceItem s WHERE s.company.id = :companyId " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ServiceItem> searchByCompanyId(@Param("companyId") Long companyId,
                                        @Param("searchTerm") String searchTerm,
                                        Pageable pageable);

    Optional<ServiceItem> findByIdAndCompanyId(Long serviceItemId, Long companyId);
}

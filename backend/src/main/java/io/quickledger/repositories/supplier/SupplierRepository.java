package io.quickledger.repositories.supplier;

import io.quickledger.entities.supplier.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByCompanyIdOrderByNameAsc(Long companyId);

    List<Supplier> findByCompanyIdAndIsActiveTrueOrderByNameAsc(Long companyId);

    Optional<Supplier> findByIdAndCompanyId(Long id, Long companyId);

    Page<Supplier> findAllByCompanyIdOrderByCreatedDateDesc(Long companyId, Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.company.id = :companyId AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.contactName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Supplier> searchSuppliers(
            @Param("companyId") Long companyId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    boolean existsByNameAndCompanyId(String name, Long companyId);
}

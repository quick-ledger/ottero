package io.quickledger.repositories;

import io.quickledger.entities.invoice.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface InvoiceRepository extends CrudRepository<Invoice, Long> {
    Optional<Invoice> findById(Long id);

    Page<Invoice> findAllByCompanyIdOrderByCreatedDateDesc(Long companyId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(id) FROM invoices WHERE company_id = :companyId AND YEAR(created_date) = YEAR(CURRENT_DATE) AND MONTH(created_date) = MONTH(CURRENT_DATE)", nativeQuery = true)
    long countMonthlyInvoicesByCompanyId(@org.springframework.data.repository.query.Param("companyId") Long companyId);
}

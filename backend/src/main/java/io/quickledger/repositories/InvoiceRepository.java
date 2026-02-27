package io.quickledger.repositories;

import io.quickledger.entities.invoice.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends CrudRepository<Invoice, Long> {
    Optional<Invoice> findById(Long id);

    Page<Invoice> findAllByCompanyIdOrderByCreatedDateDesc(Long companyId, Pageable pageable);

    @Query(value = "SELECT COUNT(id) FROM invoices WHERE company_id = :companyId AND YEAR(created_date) = YEAR(CURRENT_DATE) AND MONTH(created_date) = MONTH(CURRENT_DATE)", nativeQuery = true)
    long countMonthlyInvoicesByCompanyId(@Param("companyId") Long companyId);

    // Monthly revenue for last 6 months (for PAID invoices)
    @Query(value = """
        SELECT DATE_FORMAT(created_date, '%Y-%m') as month,
               COALESCE(SUM(total_price), 0) as revenue,
               COUNT(*) as invoice_count
        FROM invoices
        WHERE company_id = :companyId
          AND status = 'PAID'
          AND created_date >= DATE_SUB(CURRENT_DATE, INTERVAL 6 MONTH)
        GROUP BY DATE_FORMAT(created_date, '%Y-%m')
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> getMonthlyRevenueByCompanyId(@Param("companyId") Long companyId);

    // Outstanding invoices (SENT status - not yet paid)
    @Query(value = """
        SELECT COUNT(*) as count,
               COALESCE(SUM(total_price), 0) as total_amount
        FROM invoices
        WHERE company_id = :companyId
          AND status = 'SENT'
        """, nativeQuery = true)
    List<Object[]> getOutstandingInvoicesByCompanyId(@Param("companyId") Long companyId);

    // Top customers by revenue (PAID invoices)
    @Query(value = """
        SELECT c.id as customer_id,
               CONCAT(COALESCE(c.contact_name, ''), ' ', COALESCE(c.contact_surname, '')) as customer_name,
               COALESCE(SUM(i.total_price), 0) as total_revenue,
               COUNT(i.id) as invoice_count
        FROM invoices i
        JOIN clients c ON i.client_id = c.id
        WHERE i.company_id = :companyId
          AND i.status = 'PAID'
        GROUP BY c.id, c.contact_name, c.contact_surname
        ORDER BY total_revenue DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getTopCustomersByCompanyId(@Param("companyId") Long companyId, @Param("limit") int limit);

    // Find recurring invoices due for generation
    @Query("SELECT i FROM Invoice i WHERE i.isRecurring = true AND i.nextRecurringDate <= :today AND (i.recurringEndDate IS NULL OR i.recurringEndDate >= :today)")
    List<Invoice> findRecurringInvoicesDueForGeneration(@Param("today") LocalDate today);
}

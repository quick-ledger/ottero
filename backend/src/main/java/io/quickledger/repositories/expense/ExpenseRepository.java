package io.quickledger.repositories.expense;

import io.quickledger.entities.expense.Expense;
import io.quickledger.entities.expense.ExpenseCategory;
import io.quickledger.entities.expense.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByCompanyIdOrderByExpenseDateDesc(Long companyId, Pageable pageable);

    Optional<Expense> findByIdAndCompanyId(Long id, Long companyId);

    List<Expense> findByCompanyIdAndFinancialYear(Long companyId, String financialYear);

    Page<Expense> findByCompanyIdAndCategory(Long companyId, ExpenseCategory category, Pageable pageable);

    Page<Expense> findByCompanyIdAndStatus(Long companyId, ExpenseStatus status, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.company.id = :companyId AND " +
           "(LOWER(e.vendor) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.expenseDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Expense> searchExpenses(
            @Param("companyId") Long companyId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    @Query("SELECT e.category, SUM(e.amount), SUM(e.gstAmount), COUNT(e) " +
           "FROM Expense e WHERE e.company.id = :companyId AND e.financialYear = :fy " +
           "GROUP BY e.category")
    List<Object[]> getSummaryByCategory(
            @Param("companyId") Long companyId,
            @Param("fy") String financialYear);

    @Query(value = "SELECT DATE_FORMAT(expense_date, '%Y-%m'), " +
           "SUM(amount), SUM(gst_amount), COUNT(*) " +
           "FROM expenses WHERE company_id = :companyId AND financial_year = :fy " +
           "GROUP BY DATE_FORMAT(expense_date, '%Y-%m') " +
           "ORDER BY DATE_FORMAT(expense_date, '%Y-%m')", nativeQuery = true)
    List<Object[]> getMonthlySummary(
            @Param("companyId") Long companyId,
            @Param("fy") String financialYear);

    @Query("SELECT SUM(e.amount), SUM(e.gstAmount), COUNT(e) " +
           "FROM Expense e WHERE e.company.id = :companyId AND e.financialYear = :fy " +
           "AND e.taxDeductible = true")
    Object[] getTaxDeductibleTotals(
            @Param("companyId") Long companyId,
            @Param("fy") String financialYear);
}

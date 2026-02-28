package io.quickledger.repositories.expense;

import io.quickledger.entities.expense.ExpenseAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseAttachmentRepository extends JpaRepository<ExpenseAttachment, Long> {

    List<ExpenseAttachment> findAllByExpenseId(Long expenseId);

    Optional<ExpenseAttachment> findByIdAndCompanyId(Long id, Long companyId);
}

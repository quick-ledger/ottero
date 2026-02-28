package io.quickledger.services;

import io.quickledger.dto.expense.ExpenseAttachmentDto;
import io.quickledger.dto.expense.ExpenseDto;
import io.quickledger.dto.expense.ExpenseSummaryDto;
import io.quickledger.entities.Company;
import io.quickledger.entities.expense.Expense;
import io.quickledger.entities.expense.ExpenseAttachment;
import io.quickledger.entities.expense.ExpenseCategory;
import io.quickledger.entities.expense.ExpenseStatus;
import io.quickledger.mappers.expense.ExpenseAttachmentMapper;
import io.quickledger.mappers.expense.ExpenseMapper;
import io.quickledger.repositories.expense.ExpenseAttachmentRepository;
import io.quickledger.repositories.expense.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.quickledger.entities.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final ExpenseAttachmentRepository attachmentRepository;
    private final ExpenseMapper expenseMapper;
    private final ExpenseAttachmentMapper attachmentMapper;
    private final PlanService planService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ExpenseAttachmentRepository attachmentRepository,
            ExpenseMapper expenseMapper,
            ExpenseAttachmentMapper attachmentMapper,
            PlanService planService) {
        this.expenseRepository = expenseRepository;
        this.attachmentRepository = attachmentRepository;
        this.expenseMapper = expenseMapper;
        this.attachmentMapper = attachmentMapper;
        this.planService = planService;
    }

    private void validateExpenseAccess(User user) {
        planService.requireFeature(user, PlanService.Feature.EXPENSE_MANAGEMENT);
    }

    @Transactional
    public ExpenseDto createOrUpdateExpense(ExpenseDto dto, Long companyId, User user) {
        validateExpenseAccess(user);

        Expense expense;

        if (dto.getId() != null) {
            expense = expenseRepository.findByIdAndCompanyId(dto.getId(), companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found"));
            expenseMapper.updateEntityFromDto(dto, expense);
        } else {
            expense = expenseMapper.toEntity(dto);
            expense.setCompany(new Company(companyId));
            if (expense.getStatus() == null) {
                expense.setStatus(ExpenseStatus.PENDING);
            }
        }

        // Auto-calculate GST if gstClaimable and not provided
        // Australian GST is 10%, so GST = amount / 11
        if (expense.getGstClaimable() != null && expense.getGstClaimable() && expense.getAmount() != null) {
            if (dto.getGstAmount() == null) {
                expense.setGstAmount(expense.getAmount().divide(BigDecimal.valueOf(11), 2, RoundingMode.HALF_UP));
            }
            expense.setNetAmount(expense.getAmount().subtract(expense.getGstAmount()));
        } else {
            if (expense.getGstAmount() == null) {
                expense.setGstAmount(BigDecimal.ZERO);
            }
            expense.setNetAmount(expense.getAmount());
        }

        // Auto-set financial year (Australian: July 1 - June 30)
        if (expense.getExpenseDate() != null) {
            expense.setFinancialYear(calculateFinancialYear(expense.getExpenseDate()));
        }

        expense = expenseRepository.save(expense);
        return expenseMapper.toDto(expense);
    }

    public Page<ExpenseDto> getAllExpenses(Long companyId, Pageable pageable) {
        return expenseRepository.findByCompanyIdOrderByExpenseDateDesc(companyId, pageable)
                .map(expenseMapper::toDto);
    }

    public Page<ExpenseDto> searchExpenses(Long companyId, String searchTerm, Pageable pageable) {
        return expenseRepository.searchExpenses(companyId, searchTerm, pageable)
                .map(expenseMapper::toDto);
    }

    public ExpenseDto getExpenseById(Long id, Long companyId) {
        Expense expense = expenseRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));
        return expenseMapper.toDto(expense);
    }

    @Transactional
    public void deleteExpense(Long id, Long companyId, User user) {
        validateExpenseAccess(user);
        Expense expense = expenseRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    // Attachment methods
    @Transactional
    public ExpenseAttachmentDto uploadAttachment(Long expenseId, Long companyId,
            String fileName, String contentType, byte[] data, long size, User user) {
        validateExpenseAccess(user);
        Expense expense = expenseRepository.findByIdAndCompanyId(expenseId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));

        ExpenseAttachment attachment = new ExpenseAttachment();
        attachment.setExpense(expense);
        attachment.setCompany(expense.getCompany());
        attachment.setFileName(fileName);
        attachment.setContentType(contentType);
        attachment.setData(data);
        attachment.setSize(size);

        attachment = attachmentRepository.save(attachment);
        return attachmentMapper.toDto(attachment);
    }

    @Transactional(readOnly = true)
    public ExpenseAttachment getAttachmentEntity(Long attachmentId, Long companyId) {
        return attachmentRepository.findByIdAndCompanyId(attachmentId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
    }

    @Transactional
    public void deleteAttachment(Long attachmentId, Long companyId, User user) {
        validateExpenseAccess(user);
        ExpenseAttachment attachment = attachmentRepository.findByIdAndCompanyId(attachmentId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
        attachmentRepository.delete(attachment);
    }

    // Summary/Report methods
    public ExpenseSummaryDto getExpenseSummary(Long companyId, String financialYear) {
        ExpenseSummaryDto summary = new ExpenseSummaryDto();

        // Category breakdown
        List<Object[]> categoryData = expenseRepository.getSummaryByCategory(companyId, financialYear);
        List<ExpenseSummaryDto.CategorySummary> categories = categoryData.stream()
                .map(row -> {
                    ExpenseSummaryDto.CategorySummary cs = new ExpenseSummaryDto.CategorySummary();
                    ExpenseCategory cat = (ExpenseCategory) row[0];
                    cs.setCategory(cat.name());
                    cs.setCategoryDisplayName(cat.getDisplayName());
                    cs.setAmount((BigDecimal) row[1]);
                    cs.setGstAmount(row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO);
                    cs.setCount(((Number) row[3]).intValue());
                    return cs;
                })
                .collect(Collectors.toList());
        summary.setByCategory(categories);

        // Monthly breakdown
        List<Object[]> monthlyData = expenseRepository.getMonthlySummary(companyId, financialYear);
        List<ExpenseSummaryDto.MonthlySummary> monthly = monthlyData.stream()
                .map(row -> {
                    ExpenseSummaryDto.MonthlySummary ms = new ExpenseSummaryDto.MonthlySummary();
                    ms.setMonth((String) row[0]);
                    ms.setAmount((BigDecimal) row[1]);
                    ms.setGstAmount(row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO);
                    ms.setCount(((Number) row[3]).intValue());
                    return ms;
                })
                .collect(Collectors.toList());
        summary.setByMonth(monthly);

        // Totals
        Object[] totals = expenseRepository.getTaxDeductibleTotals(companyId, financialYear);
        if (totals != null && totals[0] != null) {
            summary.setTotalTaxDeductible((BigDecimal) totals[0]);
            summary.setTotalGstClaimable(totals[1] != null ? (BigDecimal) totals[1] : BigDecimal.ZERO);
            summary.setExpenseCount(((Number) totals[2]).intValue());
        } else {
            summary.setTotalTaxDeductible(BigDecimal.ZERO);
            summary.setTotalGstClaimable(BigDecimal.ZERO);
            summary.setExpenseCount(0);
        }

        // Calculate total expenses
        BigDecimal totalExpenses = categories.stream()
                .map(ExpenseSummaryDto.CategorySummary::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalExpenses(totalExpenses);

        return summary;
    }

    // Australian Financial Year: July 1 - June 30
    private String calculateFinancialYear(LocalDate date) {
        int year = date.getYear();
        if (date.getMonthValue() >= Month.JULY.getValue()) {
            return year + "-" + String.valueOf(year + 1).substring(2);
        } else {
            return (year - 1) + "-" + String.valueOf(year).substring(2);
        }
    }
}

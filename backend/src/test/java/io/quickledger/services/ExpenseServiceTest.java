package io.quickledger.services;

import io.quickledger.dto.expense.ExpenseAttachmentDto;
import io.quickledger.dto.expense.ExpenseDto;
import io.quickledger.dto.expense.ExpenseSummaryDto;
import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.entities.expense.Expense;
import io.quickledger.entities.expense.ExpenseAttachment;
import io.quickledger.entities.expense.ExpenseCategory;
import io.quickledger.entities.expense.ExpenseStatus;
import io.quickledger.mappers.expense.ExpenseAttachmentMapper;
import io.quickledger.mappers.expense.ExpenseMapper;
import io.quickledger.repositories.expense.ExpenseAttachmentRepository;
import io.quickledger.repositories.expense.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseAttachmentRepository attachmentRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private ExpenseAttachmentMapper attachmentMapper;

    private ExpenseService expenseService;

    private User advancedUser;
    private User freeUser;
    private User basicUser;
    private Company testCompany;
    private Expense testExpense;
    private ExpenseDto testExpenseDto;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(
                expenseRepository,
                attachmentRepository,
                expenseMapper,
                attachmentMapper
        );

        // Setup users with different plans
        advancedUser = new User();
        advancedUser.setId(1L);
        advancedUser.setSubscriptionPlan("Advanced");

        freeUser = new User();
        freeUser.setId(2L);
        freeUser.setSubscriptionPlan("Free");

        basicUser = new User();
        basicUser.setId(3L);
        basicUser.setSubscriptionPlan("Basic");

        // Setup company
        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Company");

        // Setup test expense
        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setCompany(testCompany);
        testExpense.setVendor("Test Vendor");
        testExpense.setExpenseDescription("Test expense");
        testExpense.setAmount(new BigDecimal("110.00"));
        testExpense.setCategory(ExpenseCategory.OFFICE_SUPPLIES);
        testExpense.setStatus(ExpenseStatus.PENDING);
        testExpense.setExpenseDate(LocalDate.of(2026, 2, 15));
        testExpense.setTaxDeductible(true);
        testExpense.setGstClaimable(true);

        // Setup test DTO
        testExpenseDto = new ExpenseDto();
        testExpenseDto.setVendor("Test Vendor");
        testExpenseDto.setExpenseDescription("Test expense");
        testExpenseDto.setAmount(new BigDecimal("110.00"));
        testExpenseDto.setCategory(ExpenseCategory.OFFICE_SUPPLIES);
        testExpenseDto.setStatus(ExpenseStatus.PENDING);
        testExpenseDto.setExpenseDate(LocalDate.of(2026, 2, 15));
        testExpenseDto.setTaxDeductible(true);
        testExpenseDto.setGstClaimable(true);
    }

    // ==================== Plan Restriction Tests ====================

    @Test
    void createExpense_withAdvancedPlan_succeeds() {
        when(expenseMapper.toEntity(any(ExpenseDto.class))).thenReturn(testExpense);
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(testExpenseDto);

        ExpenseDto result = expenseService.createOrUpdateExpense(testExpenseDto, 1L, advancedUser);

        assertNotNull(result);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createExpense_withFreePlan_throwsException() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> expenseService.createOrUpdateExpense(testExpenseDto, 1L, freeUser)
        );

        assertTrue(exception.getMessage().contains("Advanced plan"));
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpense_withBasicPlan_throwsException() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> expenseService.createOrUpdateExpense(testExpenseDto, 1L, basicUser)
        );

        assertTrue(exception.getMessage().contains("Advanced plan"));
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpense_withNullPlan_throwsException() {
        User nullPlanUser = new User();
        nullPlanUser.setId(4L);
        nullPlanUser.setSubscriptionPlan(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> expenseService.createOrUpdateExpense(testExpenseDto, 1L, nullPlanUser)
        );

        assertTrue(exception.getMessage().contains("Advanced plan"));
    }

    // ==================== CRUD Tests ====================

    @Test
    void updateExpense_withAdvancedPlan_succeeds() {
        testExpenseDto.setId(1L);
        when(expenseRepository.findByIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(testExpenseDto);

        ExpenseDto result = expenseService.createOrUpdateExpense(testExpenseDto, 1L, advancedUser);

        assertNotNull(result);
        verify(expenseMapper).updateEntityFromDto(any(ExpenseDto.class), any(Expense.class));
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void updateExpense_notFound_throwsException() {
        testExpenseDto.setId(999L);
        when(expenseRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> expenseService.createOrUpdateExpense(testExpenseDto, 1L, advancedUser)
        );
    }

    @Test
    void getExpenseById_found_returnsExpense() {
        when(expenseRepository.findByIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(testExpense));
        when(expenseMapper.toDto(testExpense)).thenReturn(testExpenseDto);

        ExpenseDto result = expenseService.getExpenseById(1L, 1L);

        assertNotNull(result);
        assertEquals("Test Vendor", result.getVendor());
    }

    @Test
    void getExpenseById_notFound_throwsException() {
        when(expenseRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> expenseService.getExpenseById(999L, 1L)
        );
    }

    @Test
    void getAllExpenses_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> expensePage = new PageImpl<>(List.of(testExpense));
        when(expenseRepository.findByCompanyIdOrderByExpenseDateDesc(1L, pageable)).thenReturn(expensePage);
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(testExpenseDto);

        Page<ExpenseDto> result = expenseService.getAllExpenses(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchExpenses_returnsMatchingPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> expensePage = new PageImpl<>(List.of(testExpense));
        when(expenseRepository.searchExpenses(1L, "Test", pageable)).thenReturn(expensePage);
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(testExpenseDto);

        Page<ExpenseDto> result = expenseService.searchExpenses(1L, "Test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deleteExpense_withAdvancedPlan_succeeds() {
        when(expenseRepository.findByIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(testExpense));

        expenseService.deleteExpense(1L, 1L, advancedUser);

        verify(expenseRepository).delete(testExpense);
    }

    @Test
    void deleteExpense_withFreePlan_throwsException() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> expenseService.deleteExpense(1L, 1L, freeUser)
        );

        assertTrue(exception.getMessage().contains("Advanced plan"));
        verify(expenseRepository, never()).delete(any(Expense.class));
    }

    @Test
    void deleteExpense_notFound_throwsException() {
        when(expenseRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> expenseService.deleteExpense(999L, 1L, advancedUser)
        );
    }

    // ==================== Attachment Tests ====================

    @Test
    void uploadAttachment_withAdvancedPlan_succeeds() {
        ExpenseAttachment attachment = new ExpenseAttachment();
        attachment.setId(1L);
        attachment.setFileName("receipt.pdf");
        attachment.setContentType("application/pdf");
        attachment.setSize(1024L);

        ExpenseAttachmentDto attachmentDto = new ExpenseAttachmentDto();
        attachmentDto.setId(1L);
        attachmentDto.setFileName("receipt.pdf");

        when(expenseRepository.findByIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(testExpense));
        when(attachmentRepository.save(any(ExpenseAttachment.class))).thenReturn(attachment);
        when(attachmentMapper.toDto(any(ExpenseAttachment.class))).thenReturn(attachmentDto);

        ExpenseAttachmentDto result = expenseService.uploadAttachment(
                1L, 1L, "receipt.pdf", "application/pdf", new byte[]{1, 2, 3}, 1024L, advancedUser
        );

        assertNotNull(result);
        assertEquals("receipt.pdf", result.getFileName());
        verify(attachmentRepository).save(any(ExpenseAttachment.class));
    }

    @Test
    void uploadAttachment_withFreePlan_throwsException() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> expenseService.uploadAttachment(
                        1L, 1L, "receipt.pdf", "application/pdf", new byte[]{1, 2, 3}, 1024L, freeUser
                )
        );

        assertTrue(exception.getMessage().contains("Advanced plan"));
        verify(attachmentRepository, never()).save(any(ExpenseAttachment.class));
    }

    @Test
    void uploadAttachment_expenseNotFound_throwsException() {
        when(expenseRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> expenseService.uploadAttachment(
                        999L, 1L, "receipt.pdf", "application/pdf", new byte[]{1, 2, 3}, 1024L, advancedUser
                )
        );
    }

    @Test
    void getAttachmentEntity_found_returnsAttachment() {
        ExpenseAttachment attachment = new ExpenseAttachment();
        attachment.setId(1L);
        attachment.setFileName("receipt.pdf");

        when(attachmentRepository.findByIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(attachment));

        ExpenseAttachment result = expenseService.getAttachmentEntity(1L, 1L);

        assertNotNull(result);
        assertEquals("receipt.pdf", result.getFileName());
    }

    @Test
    void getAttachmentEntity_notFound_throwsException() {
        when(attachmentRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> expenseService.getAttachmentEntity(999L, 1L)
        );
    }

    @Test
    void deleteAttachment_withAdvancedPlan_succeeds() {
        ExpenseAttachment attachment = new ExpenseAttachment();
        attachment.setId(1L);

        when(attachmentRepository.findByIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(attachment));

        expenseService.deleteAttachment(1L, 1L, advancedUser);

        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void deleteAttachment_withBasicPlan_throwsException() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> expenseService.deleteAttachment(1L, 1L, basicUser)
        );

        assertTrue(exception.getMessage().contains("Advanced plan"));
        verify(attachmentRepository, never()).delete(any(ExpenseAttachment.class));
    }

    // ==================== GST Calculation Tests ====================

    @Test
    void createExpense_withGstClaimable_calculatesGstAutomatically() {
        testExpenseDto.setGstClaimable(true);
        testExpenseDto.setGstAmount(null); // Not provided, should be auto-calculated

        Expense expenseToSave = new Expense();
        expenseToSave.setAmount(new BigDecimal("110.00"));
        expenseToSave.setGstClaimable(true);

        when(expenseMapper.toEntity(any(ExpenseDto.class))).thenReturn(expenseToSave);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            // Verify GST calculation: 110 / 11 = 10.00
            assertEquals(new BigDecimal("10.00"), saved.getGstAmount());
            // Verify net amount: 110 - 10 = 100.00
            assertEquals(new BigDecimal("100.00"), saved.getNetAmount());
            return saved;
        });
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(testExpenseDto);

        expenseService.createOrUpdateExpense(testExpenseDto, 1L, advancedUser);

        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createExpense_withoutGstClaimable_setsGstToZero() {
        testExpenseDto.setGstClaimable(false);
        testExpenseDto.setGstAmount(null);

        Expense expenseToSave = new Expense();
        expenseToSave.setAmount(new BigDecimal("100.00"));
        expenseToSave.setGstClaimable(false);

        when(expenseMapper.toEntity(any(ExpenseDto.class))).thenReturn(expenseToSave);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            assertEquals(BigDecimal.ZERO, saved.getGstAmount());
            assertEquals(new BigDecimal("100.00"), saved.getNetAmount());
            return saved;
        });
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(testExpenseDto);

        expenseService.createOrUpdateExpense(testExpenseDto, 1L, advancedUser);

        verify(expenseRepository).save(any(Expense.class));
    }

    // ==================== Financial Year Tests ====================

    @Test
    void createExpense_inJuly_setsCorrectFinancialYear() {
        testExpenseDto.setExpenseDate(LocalDate.of(2025, 7, 15)); // July 2025

        Expense expenseToSave = new Expense();
        expenseToSave.setAmount(new BigDecimal("100.00"));
        expenseToSave.setExpenseDate(LocalDate.of(2025, 7, 15));
        expenseToSave.setGstClaimable(false);

        when(expenseMapper.toEntity(any(ExpenseDto.class))).thenReturn(expenseToSave);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            // July 2025 is in FY 2025-26
            assertEquals("2025-26", saved.getFinancialYear());
            return saved;
        });
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(testExpenseDto);

        expenseService.createOrUpdateExpense(testExpenseDto, 1L, advancedUser);

        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createExpense_inJune_setsCorrectFinancialYear() {
        testExpenseDto.setExpenseDate(LocalDate.of(2026, 6, 15)); // June 2026

        Expense expenseToSave = new Expense();
        expenseToSave.setAmount(new BigDecimal("100.00"));
        expenseToSave.setExpenseDate(LocalDate.of(2026, 6, 15));
        expenseToSave.setGstClaimable(false);

        when(expenseMapper.toEntity(any(ExpenseDto.class))).thenReturn(expenseToSave);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            // June 2026 is in FY 2025-26
            assertEquals("2025-26", saved.getFinancialYear());
            return saved;
        });
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(testExpenseDto);

        expenseService.createOrUpdateExpense(testExpenseDto, 1L, advancedUser);

        verify(expenseRepository).save(any(Expense.class));
    }

    // ==================== Summary Tests ====================

    @Test
    void getExpenseSummary_returnsCorrectTotals() {
        // Mock category data
        List<Object[]> categoryData = new ArrayList<>();
        categoryData.add(new Object[]{ExpenseCategory.OFFICE_SUPPLIES, new BigDecimal("500.00"), new BigDecimal("45.45"), 5L});
        when(expenseRepository.getSummaryByCategory(1L, "2025-26")).thenReturn(categoryData);

        // Mock monthly data
        List<Object[]> monthlyData = new ArrayList<>();
        monthlyData.add(new Object[]{"2025-07", new BigDecimal("200.00"), new BigDecimal("18.18"), 2L});
        when(expenseRepository.getMonthlySummary(1L, "2025-26")).thenReturn(monthlyData);

        // Mock totals
        Object[] totals = new Object[]{new BigDecimal("400.00"), new BigDecimal("36.36"), 4L};
        when(expenseRepository.getTaxDeductibleTotals(1L, "2025-26")).thenReturn(totals);

        ExpenseSummaryDto result = expenseService.getExpenseSummary(1L, "2025-26");

        assertNotNull(result);
        assertEquals(1, result.getByCategory().size());
        assertEquals("OFFICE_SUPPLIES", result.getByCategory().get(0).getCategory());
        assertEquals(new BigDecimal("500.00"), result.getByCategory().get(0).getAmount());
        assertEquals(1, result.getByMonth().size());
        assertEquals(new BigDecimal("400.00"), result.getTotalTaxDeductible());
        assertEquals(new BigDecimal("36.36"), result.getTotalGstClaimable());
        assertEquals(4, result.getExpenseCount());
    }

    @Test
    void getExpenseSummary_withNoData_returnsZeroTotals() {
        when(expenseRepository.getSummaryByCategory(1L, "2025-26")).thenReturn(Collections.emptyList());
        when(expenseRepository.getMonthlySummary(1L, "2025-26")).thenReturn(Collections.emptyList());
        when(expenseRepository.getTaxDeductibleTotals(1L, "2025-26")).thenReturn(null);

        ExpenseSummaryDto result = expenseService.getExpenseSummary(1L, "2025-26");

        assertNotNull(result);
        assertTrue(result.getByCategory().isEmpty());
        assertTrue(result.getByMonth().isEmpty());
        assertEquals(BigDecimal.ZERO, result.getTotalTaxDeductible());
        assertEquals(BigDecimal.ZERO, result.getTotalGstClaimable());
        assertEquals(0, result.getExpenseCount());
    }
}

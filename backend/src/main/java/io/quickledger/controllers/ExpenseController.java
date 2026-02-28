package io.quickledger.controllers;

import io.quickledger.dto.expense.ExpenseAttachmentDto;
import io.quickledger.dto.expense.ExpenseDto;
import io.quickledger.dto.expense.ExpenseSummaryDto;
import io.quickledger.entities.User;
import io.quickledger.entities.expense.ExpenseAttachment;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.ExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/companies/{companyId}/expenses")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(
            @PathVariable Long companyId,
            @RequestBody ExpenseDto dto,
            @UserIdAuth final User user) {
        dto.setCompanyId(companyId);
        ExpenseDto created = expenseService.createOrUpdateExpense(dto, companyId, user);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseDto>> getAllExpenses(
            @PathVariable Long companyId,
            Pageable pageable,
            @UserIdAuth final User user) {
        Page<ExpenseDto> expenses = expenseService.getAllExpenses(companyId, pageable);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ExpenseDto>> searchExpenses(
            @PathVariable Long companyId,
            @RequestParam String searchTerm,
            Pageable pageable,
            @UserIdAuth final User user) {
        Page<ExpenseDto> expenses = expenseService.searchExpenses(companyId, searchTerm, pageable);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpenseById(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @UserIdAuth final User user) {
        ExpenseDto expense = expenseService.getExpenseById(id, companyId);
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @RequestBody ExpenseDto dto,
            @UserIdAuth final User user) {
        dto.setId(id);
        dto.setCompanyId(companyId);
        ExpenseDto updated = expenseService.createOrUpdateExpense(dto, companyId, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @UserIdAuth final User user) {
        expenseService.deleteExpense(id, companyId, user);
        return ResponseEntity.noContent().build();
    }

    // Attachments
    @PostMapping("/{expenseId}/attachments")
    public ResponseEntity<ExpenseAttachmentDto> uploadAttachment(
            @PathVariable Long companyId,
            @PathVariable Long expenseId,
            @RequestParam("file") MultipartFile file,
            @UserIdAuth final User user) {
        try {
            ExpenseAttachmentDto attachment = expenseService.uploadAttachment(
                    expenseId, companyId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes(),
                    file.getSize(),
                    user);
            return ResponseEntity.ok(attachment);
        } catch (IOException e) {
            logger.error("Failed to upload attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{expenseId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable Long companyId,
            @PathVariable Long expenseId,
            @PathVariable Long attachmentId) {
        ExpenseAttachment attachment = expenseService.getAttachmentEntity(attachmentId, companyId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .body(attachment.getData());
    }

    @DeleteMapping("/{expenseId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long companyId,
            @PathVariable Long expenseId,
            @PathVariable Long attachmentId,
            @UserIdAuth final User user) {
        expenseService.deleteAttachment(attachmentId, companyId, user);
        return ResponseEntity.noContent().build();
    }

    // Reports/Summary
    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryDto> getExpenseSummary(
            @PathVariable Long companyId,
            @RequestParam(required = false) String financialYear,
            @UserIdAuth final User user) {
        // Default to current financial year if not specified
        if (financialYear == null || financialYear.isEmpty()) {
            LocalDate now = LocalDate.now();
            financialYear = now.getMonthValue() >= 7
                    ? now.getYear() + "-" + String.valueOf(now.getYear() + 1).substring(2)
                    : (now.getYear() - 1) + "-" + String.valueOf(now.getYear()).substring(2);
        }
        ExpenseSummaryDto summary = expenseService.getExpenseSummary(companyId, financialYear);
        return ResponseEntity.ok(summary);
    }
}

package io.quickledger.controllers;

import io.quickledger.dto.job.*;
import io.quickledger.entities.User;
import io.quickledger.entities.job.JobAttachment;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.JobService;
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

@RestController
@RequestMapping("/companies/{companyId}/jobs")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // CRUD for Jobs
    @PostMapping
    public ResponseEntity<JobDto> createJob(
            @PathVariable Long companyId,
            @RequestBody JobDto dto,
            @UserIdAuth final User user) {
        dto.setCompanyId(companyId);
        JobDto created = jobService.createOrUpdateJob(dto, companyId, user);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<Page<JobDto>> getAllJobs(
            @PathVariable Long companyId,
            Pageable pageable,
            @UserIdAuth final User user) {
        Page<JobDto> jobs = jobService.getAllJobs(companyId, pageable);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<JobDto>> searchJobs(
            @PathVariable Long companyId,
            @RequestParam String searchTerm,
            Pageable pageable,
            @UserIdAuth final User user) {
        Page<JobDto> jobs = jobService.searchJobs(companyId, searchTerm, pageable);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobById(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @UserIdAuth final User user) {
        JobDto job = jobService.getJobById(id, companyId);
        return ResponseEntity.ok(job);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobDto> updateJob(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @RequestBody JobDto dto,
            @UserIdAuth final User user) {
        dto.setId(id);
        dto.setCompanyId(companyId);
        JobDto updated = jobService.createOrUpdateJob(dto, companyId, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @UserIdAuth final User user) {
        jobService.deleteJob(id, companyId, user);
        return ResponseEntity.noContent().build();
    }

    // Notes
    @PostMapping("/{jobId}/notes")
    public ResponseEntity<JobNoteDto> addNote(
            @PathVariable Long companyId,
            @PathVariable Long jobId,
            @RequestBody JobNoteDto dto,
            @UserIdAuth final User user) {
        JobNoteDto created = jobService.addNote(jobId, companyId, dto, user);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{jobId}/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long companyId,
            @PathVariable Long jobId,
            @PathVariable Long noteId,
            @UserIdAuth final User user) {
        jobService.deleteNote(noteId, companyId, user);
        return ResponseEntity.noContent().build();
    }

    // Attachments
    @PostMapping("/{jobId}/attachments")
    public ResponseEntity<JobAttachmentDto> uploadAttachment(
            @PathVariable Long companyId,
            @PathVariable Long jobId,
            @RequestParam("file") MultipartFile file,
            @UserIdAuth final User user) {
        try {
            JobAttachmentDto attachment = jobService.uploadAttachment(
                    jobId, companyId,
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

    @GetMapping("/{jobId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable Long companyId,
            @PathVariable Long jobId,
            @PathVariable Long attachmentId) {
        JobAttachment attachment = jobService.getAttachmentEntity(attachmentId, companyId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .body(attachment.getData());
    }

    @DeleteMapping("/{jobId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long companyId,
            @PathVariable Long jobId,
            @PathVariable Long attachmentId,
            @UserIdAuth final User user) {
        jobService.deleteAttachment(attachmentId, companyId, user);
        return ResponseEntity.noContent().build();
    }

    // Quote Links
    @PostMapping("/{jobId}/quotes/{quoteId}")
    public ResponseEntity<Void> linkQuote(
            @PathVariable Long companyId,
            @PathVariable Long jobId,
            @PathVariable Long quoteId,
            @UserIdAuth final User user) {
        jobService.linkQuote(jobId, quoteId, companyId, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{jobId}/quotes/{quoteId}")
    public ResponseEntity<Void> unlinkQuote(
            @PathVariable Long companyId,
            @PathVariable Long jobId,
            @PathVariable Long quoteId,
            @UserIdAuth final User user) {
        jobService.unlinkQuote(jobId, quoteId, companyId, user);
        return ResponseEntity.noContent().build();
    }

    // Invoice Links
    @PostMapping("/{jobId}/invoices/{invoiceId}")
    public ResponseEntity<Void> linkInvoice(
            @PathVariable Long companyId,
            @PathVariable Long jobId,
            @PathVariable Long invoiceId,
            @UserIdAuth final User user) {
        jobService.linkInvoice(jobId, invoiceId, companyId, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{jobId}/invoices/{invoiceId}")
    public ResponseEntity<Void> unlinkInvoice(
            @PathVariable Long companyId,
            @PathVariable Long jobId,
            @PathVariable Long invoiceId,
            @UserIdAuth final User user) {
        jobService.unlinkInvoice(jobId, invoiceId, companyId, user);
        return ResponseEntity.noContent().build();
    }
}

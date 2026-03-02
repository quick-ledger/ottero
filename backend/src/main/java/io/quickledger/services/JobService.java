package io.quickledger.services;

import io.quickledger.dto.job.*;
import io.quickledger.entities.Client;
import io.quickledger.entities.Company;
import io.quickledger.entities.SequenceConfig;
import io.quickledger.entities.User;
import io.quickledger.entities.invoice.Invoice;
import io.quickledger.entities.job.*;
import io.quickledger.entities.quote.Quote;
import io.quickledger.mappers.job.*;
import io.quickledger.repositories.ClientRepository;
import io.quickledger.repositories.InvoiceRepository;
import io.quickledger.repositories.SequenceConfigRepository;
import io.quickledger.repositories.job.*;
import io.quickledger.repositories.quote.QuoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final JobNoteRepository noteRepository;
    private final JobAttachmentRepository attachmentRepository;
    private final QuoteRepository quoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final SequenceConfigRepository sequenceConfigRepository;
    private final JobMapper jobMapper;
    private final JobNoteMapper noteMapper;
    private final JobAttachmentMapper attachmentMapper;
    private final PlanService planService;

    public JobService(
            JobRepository jobRepository,
            JobNoteRepository noteRepository,
            JobAttachmentRepository attachmentRepository,
            QuoteRepository quoteRepository,
            InvoiceRepository invoiceRepository,
            ClientRepository clientRepository,
            SequenceConfigRepository sequenceConfigRepository,
            JobMapper jobMapper,
            JobNoteMapper noteMapper,
            JobAttachmentMapper attachmentMapper,
            PlanService planService) {
        this.jobRepository = jobRepository;
        this.noteRepository = noteRepository;
        this.attachmentRepository = attachmentRepository;
        this.quoteRepository = quoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.sequenceConfigRepository = sequenceConfigRepository;
        this.jobMapper = jobMapper;
        this.noteMapper = noteMapper;
        this.attachmentMapper = attachmentMapper;
        this.planService = planService;
    }

    private void validateJobAccess(User user) {
        planService.requireFeature(user, PlanService.Feature.JOB_MANAGEMENT);
    }

    @Transactional
    public JobDto createOrUpdateJob(JobDto dto, Long companyId, User user) {
        validateJobAccess(user);

        Job job;

        if (dto.getId() != null) {
            job = jobRepository.findByIdAndCompanyId(dto.getId(), companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Job not found"));
            jobMapper.updateEntityFromDto(dto, job);
        } else {
            job = jobMapper.toEntity(dto);
            job.setCompany(new Company(companyId));
            if (job.getStatus() == null) {
                job.setStatus(JobStatus.SCHEDULED);
            }
            // Generate job number
            job.setJobNumber(generateJobNumber(companyId));
        }

        // Set client if provided
        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new EntityNotFoundException("Client not found"));
            job.setClient(client);
        } else {
            job.setClient(null);
        }

        job = jobRepository.save(job);
        return toFullDto(job);
    }

    private String generateJobNumber(Long companyId) {
        Optional<SequenceConfig> sequenceConfigOptional = sequenceConfigRepository
                .findByEntityTypeAndCompanyIdForUpdate(SequenceConfig.EntityType.JOB, companyId);

        if (sequenceConfigOptional.isEmpty()) {
            // Create default sequence config for JOB if not exists
            SequenceConfig config = new SequenceConfig();
            config.setEntityType(SequenceConfig.EntityType.JOB);
            config.setCompanyId(companyId);
            config.setPrefix("JOB-");
            config.setPostfix("");
            config.setCurrentNumber(0);
            config.setNumberPadding(4);
            sequenceConfigRepository.save(config);

            return "JOB-0001";
        }

        SequenceConfig sequenceConfig = sequenceConfigOptional.get();
        Integer currentNumber = sequenceConfig.getCurrentNumber();
        int newJobSequence = currentNumber + 1;

        String newJobNumber = io.quickledger.utils.SequenceNumberFormatter.format(
                sequenceConfig.getPrefix(),
                newJobSequence,
                sequenceConfig.getPostfix(),
                sequenceConfig.getNumberPadding(),
                LocalDate.now());

        sequenceConfig.setCurrentNumber(newJobSequence);
        sequenceConfigRepository.save(sequenceConfig);

        return newJobNumber;
    }

    public Page<JobDto> getAllJobs(Long companyId, Pageable pageable) {
        return jobRepository.findByCompanyIdOrderByScheduledDateDesc(companyId, pageable)
                .map(this::toFullDto);
    }

    public Page<JobDto> searchJobs(Long companyId, String searchTerm, Pageable pageable) {
        return jobRepository.searchJobs(companyId, searchTerm, pageable)
                .map(this::toFullDto);
    }

    public JobDto getJobById(Long id, Long companyId) {
        Job job = jobRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));
        return toFullDto(job);
    }

    @Transactional
    public void deleteJob(Long id, Long companyId, User user) {
        validateJobAccess(user);
        Job job = jobRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));
        jobRepository.delete(job);
    }

    // Job Notes
    @Transactional
    public JobNoteDto addNote(Long jobId, Long companyId, JobNoteDto dto, User user) {
        validateJobAccess(user);
        Job job = jobRepository.findByIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));

        JobNote note = new JobNote();
        note.setJob(job);
        note.setCompany(job.getCompany());
        note.setNoteText(dto.getNoteText());
        note.setNoteDate(dto.getNoteDate() != null ? dto.getNoteDate() : LocalDate.now());

        note = noteRepository.save(note);
        return noteMapper.toDto(note);
    }

    @Transactional
    public void deleteNote(Long noteId, Long companyId, User user) {
        validateJobAccess(user);
        JobNote note = noteRepository.findByIdAndCompanyId(noteId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found"));
        noteRepository.delete(note);
    }

    // Attachments
    @Transactional
    public JobAttachmentDto uploadAttachment(Long jobId, Long companyId,
            String fileName, String contentType, byte[] data, long size, User user) {
        validateJobAccess(user);
        Job job = jobRepository.findByIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));

        JobAttachment attachment = new JobAttachment();
        attachment.setJob(job);
        attachment.setCompany(job.getCompany());
        attachment.setFileName(fileName);
        attachment.setContentType(contentType);
        attachment.setData(data);
        attachment.setSize(size);

        attachment = attachmentRepository.save(attachment);
        return attachmentMapper.toDto(attachment);
    }

    @Transactional(readOnly = true)
    public JobAttachment getAttachmentEntity(Long attachmentId, Long companyId) {
        return attachmentRepository.findByIdAndCompanyId(attachmentId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
    }

    @Transactional
    public void deleteAttachment(Long attachmentId, Long companyId, User user) {
        validateJobAccess(user);
        JobAttachment attachment = attachmentRepository.findByIdAndCompanyId(attachmentId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
        attachmentRepository.delete(attachment);
    }

    // Link/Unlink Quotes
    @Transactional
    public void linkQuote(Long jobId, Long quoteId, Long companyId, User user) {
        validateJobAccess(user);
        Job job = jobRepository.findByIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));
        Quote quote = quoteRepository.findByIdAndCompanyId(quoteId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found"));

        job.getLinkedQuotes().add(quote);
        jobRepository.save(job);
    }

    @Transactional
    public void unlinkQuote(Long jobId, Long quoteId, Long companyId, User user) {
        validateJobAccess(user);
        Job job = jobRepository.findByIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));
        job.getLinkedQuotes().removeIf(q -> q.getId().equals(quoteId));
        jobRepository.save(job);
    }

    // Link/Unlink Invoices
    @Transactional
    public void linkInvoice(Long jobId, Long invoiceId, Long companyId, User user) {
        validateJobAccess(user);
        Job job = jobRepository.findByIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(invoiceId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));

        job.getLinkedInvoices().add(invoice);
        jobRepository.save(job);
    }

    @Transactional
    public void unlinkInvoice(Long jobId, Long invoiceId, Long companyId, User user) {
        validateJobAccess(user);
        Job job = jobRepository.findByIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));
        job.getLinkedInvoices().removeIf(i -> i.getId().equals(invoiceId));
        jobRepository.save(job);
    }

    // Helper to build full DTO with linked entities
    private JobDto toFullDto(Job job) {
        JobDto dto = jobMapper.toDto(job);

        // Map linked quotes
        dto.setLinkedQuotes(job.getLinkedQuotes().stream()
                .map(q -> {
                    LinkedQuoteDto lq = new LinkedQuoteDto();
                    lq.setId(q.getId());
                    lq.setQuoteNumber(q.getQuoteNumber());
                    lq.setStatus(q.getStatus().name());
                    lq.setTotalPrice(q.getTotalPrice());
                    return lq;
                })
                .collect(Collectors.toList()));

        // Map linked invoices
        dto.setLinkedInvoices(job.getLinkedInvoices().stream()
                .map(i -> {
                    LinkedInvoiceDto li = new LinkedInvoiceDto();
                    li.setId(i.getId());
                    li.setInvoiceNumber(i.getInvoiceNumber());
                    li.setStatus(i.getStatus().name());
                    li.setTotalPrice(i.getTotalPrice());
                    return li;
                })
                .collect(Collectors.toList()));

        return dto;
    }
}

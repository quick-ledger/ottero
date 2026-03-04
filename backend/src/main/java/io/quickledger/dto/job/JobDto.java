package io.quickledger.dto.job;

import io.quickledger.entities.job.JobStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class JobDto {

    private Long id;
    private Long companyId;
    private Long clientId;

    // Client info (denormalized for display)
    private String clientName;
    private String clientEmail;
    private String clientPhone;

    private String jobNumber;
    private String title;
    private String jobDescription;
    private String location;
    private JobStatus status;
    private LocalDate scheduledDate;
    private LocalDate completionDate;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    private List<JobNoteDto> notes;
    private List<JobAttachmentDto> attachments;
    private List<JobTimeEntryDto> timeEntries;
    private List<LinkedQuoteDto> linkedQuotes;
    private List<LinkedInvoiceDto> linkedInvoices;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public List<JobNoteDto> getNotes() {
        return notes;
    }

    public void setNotes(List<JobNoteDto> notes) {
        this.notes = notes;
    }

    public List<JobAttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<JobAttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public List<JobTimeEntryDto> getTimeEntries() {
        return timeEntries;
    }

    public void setTimeEntries(List<JobTimeEntryDto> timeEntries) {
        this.timeEntries = timeEntries;
    }

    public List<LinkedQuoteDto> getLinkedQuotes() {
        return linkedQuotes;
    }

    public void setLinkedQuotes(List<LinkedQuoteDto> linkedQuotes) {
        this.linkedQuotes = linkedQuotes;
    }

    public List<LinkedInvoiceDto> getLinkedInvoices() {
        return linkedInvoices;
    }

    public void setLinkedInvoices(List<LinkedInvoiceDto> linkedInvoices) {
        this.linkedInvoices = linkedInvoices;
    }
}

package io.quickledger.entities.job;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Client;
import io.quickledger.entities.Company;
import io.quickledger.entities.invoice.Invoice;
import io.quickledger.entities.quote.Quote;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "jobs")
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "job_number", nullable = false, length = 100)
    private String jobNumber;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "job_description", length = 4000)
    private String jobDescription;

    @Column(name = "location", length = 1000)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private JobStatus status = JobStatus.SCHEDULED;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("noteDate DESC, createdDate DESC")
    private List<JobNote> notes = new ArrayList<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("entryDate DESC, createdDate DESC")
    private List<JobTimeEntry> timeEntries = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "job_quotes",
        joinColumns = @JoinColumn(name = "job_id"),
        inverseJoinColumns = @JoinColumn(name = "quote_id")
    )
    private Set<Quote> linkedQuotes = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "job_invoices",
        joinColumns = @JoinColumn(name = "job_id"),
        inverseJoinColumns = @JoinColumn(name = "invoice_id")
    )
    private Set<Invoice> linkedInvoices = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
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

    public List<JobNote> getNotes() {
        return notes;
    }

    public void setNotes(List<JobNote> notes) {
        this.notes = notes;
    }

    public List<JobAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<JobAttachment> attachments) {
        this.attachments = attachments;
    }

    public Set<Quote> getLinkedQuotes() {
        return linkedQuotes;
    }

    public void setLinkedQuotes(Set<Quote> linkedQuotes) {
        this.linkedQuotes = linkedQuotes;
    }

    public Set<Invoice> getLinkedInvoices() {
        return linkedInvoices;
    }

    public void setLinkedInvoices(Set<Invoice> linkedInvoices) {
        this.linkedInvoices = linkedInvoices;
    }

    public List<JobTimeEntry> getTimeEntries() {
        return timeEntries;
    }

    public void setTimeEntries(List<JobTimeEntry> timeEntries) {
        this.timeEntries = timeEntries;
    }
}

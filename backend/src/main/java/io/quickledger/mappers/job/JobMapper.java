package io.quickledger.mappers.job;

import io.quickledger.dto.job.JobDto;
import io.quickledger.entities.job.Job;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {JobNoteMapper.class, JobAttachmentMapper.class, JobTimeEntryMapper.class})
public interface JobMapper {

    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(target = "clientName", expression = "java(job.getClient() != null ? (job.getClient().getContactName() + \" \" + job.getClient().getContactSurname()).trim() : null)")
    @Mapping(source = "client.email", target = "clientEmail")
    @Mapping(source = "client.phone", target = "clientPhone")
    @Mapping(target = "linkedQuotes", ignore = true)
    @Mapping(target = "linkedInvoices", ignore = true)
    JobDto toDto(Job job);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "timeEntries", ignore = true)
    @Mapping(target = "linkedQuotes", ignore = true)
    @Mapping(target = "linkedInvoices", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Job toEntity(JobDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "timeEntries", ignore = true)
    @Mapping(target = "linkedQuotes", ignore = true)
    @Mapping(target = "linkedInvoices", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "jobNumber", ignore = true)
    void updateEntityFromDto(JobDto dto, @MappingTarget Job job);
}

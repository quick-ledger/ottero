package io.quickledger.mappers.job;

import io.quickledger.dto.job.JobAttachmentDto;
import io.quickledger.entities.job.JobAttachment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobAttachmentMapper {

    JobAttachmentDto toDto(JobAttachment attachment);
}

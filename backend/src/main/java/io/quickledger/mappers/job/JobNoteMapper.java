package io.quickledger.mappers.job;

import io.quickledger.dto.job.JobNoteDto;
import io.quickledger.entities.job.JobNote;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobNoteMapper {

    JobNoteDto toDto(JobNote note);
}

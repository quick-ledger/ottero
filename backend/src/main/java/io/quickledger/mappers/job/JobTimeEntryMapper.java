package io.quickledger.mappers.job;

import io.quickledger.dto.job.JobTimeEntryDto;
import io.quickledger.entities.job.JobTimeEntry;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobTimeEntryMapper {

    JobTimeEntryDto toDto(JobTimeEntry entity);
}

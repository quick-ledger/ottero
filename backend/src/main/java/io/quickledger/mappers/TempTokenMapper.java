package io.quickledger.mappers;

import io.quickledger.dto.TempTokenDto;
import io.quickledger.entities.TempToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TempTokenMapper {
    TempTokenMapper INSTANCE = Mappers.getMapper(TempTokenMapper.class);

    TempTokenDto toDto(TempToken tempToken);

    TempToken toEntity(TempTokenDto tempTokenDto);
}

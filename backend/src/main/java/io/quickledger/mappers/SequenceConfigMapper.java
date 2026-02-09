package io.quickledger.mappers;

import io.quickledger.dto.SequenceConfigDTO;
import io.quickledger.entities.SequenceConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SequenceConfigMapper {
    SequenceConfigMapper INSTANCE = Mappers.getMapper(SequenceConfigMapper.class);

    @Mapping(source = "entityType", target = "entityType", qualifiedByName = "entityTypeToString")
    SequenceConfigDTO toDto(SequenceConfig sequenceConfig);

    @Mapping(source = "entityType", target = "entityType", qualifiedByName = "stringToEntityType")
    SequenceConfig toEntity(SequenceConfigDTO sequenceConfigDTO);

    @Named("entityTypeToString")
    public static String entityTypeToString(SequenceConfig.EntityType entityType) {
        return entityType.name();
    }

    @Named("stringToEntityType")
    public static SequenceConfig.EntityType stringToEntityType(String entityType) {
        return SequenceConfig.EntityType.valueOf(entityType);
    }
}
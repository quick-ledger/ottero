package io.quickledger.mappers;

import io.quickledger.dto.KcEventDto;
import io.quickledger.entities.KcEvent;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface KcEventMapper {

    KcEventDto toDto(KcEvent kcEvent);

    KcEvent toEntity(KcEventDto kcEventDto);

    default KcEvent mapToKcEvent(Map<String, Object> messageData) {
        KcEvent kcEvent = new KcEvent();
        kcEvent.setEventType((String) messageData.get("eventType"));
        kcEvent.setEventDetails(((Map)messageData.get("eventDetails")).toString());

        String eventDateTimeStr = (String) messageData.get("eventDateTime");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime eventDateTime = LocalDateTime.parse(eventDateTimeStr, formatter);
        kcEvent.setEventDateTime(eventDateTime);
        kcEvent.setClientID((String) messageData.get("clientId"));
        kcEvent.setIpAddress((String) messageData.get("ipAddress"));

        return kcEvent;
    }

    KcEventMapper INSTANCE = Mappers.getMapper(KcEventMapper.class);
}
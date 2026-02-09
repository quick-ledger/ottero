package io.quickledger.services.serviceitem;

import io.quickledger.dto.serviceitem.ServiceItemAttributeValueDto;
import io.quickledger.entities.serviceitem.ServiceItemAttributeValue;
import io.quickledger.mappers.serviceitem.ServiceItemAttributeValueMapper;
import org.springframework.stereotype.Service;
import io.quickledger.repositories.serviceitem.ServiceItemAttributeValueRepository;

@Service
public class ServiceItemAttributeValueService {

    private final ServiceItemAttributeValueRepository serviceItemAttributeValueRepository;
    private final ServiceItemAttributeValueMapper serviceItemAttributeValueMapper;

    public ServiceItemAttributeValueService(ServiceItemAttributeValueRepository serviceItemAttributeValueRepository, ServiceItemAttributeValueMapper serviceItemAttributeValueMapper) {
        this.serviceItemAttributeValueRepository = serviceItemAttributeValueRepository;
        this.serviceItemAttributeValueMapper = serviceItemAttributeValueMapper;
    }

    public ServiceItemAttributeValueDto saveAttributeValue(ServiceItemAttributeValueDto serviceItemAttributeValueDto) {
        ServiceItemAttributeValue serviceItemAttributeValue = serviceItemAttributeValueMapper.toEntity(serviceItemAttributeValueDto);
        ServiceItemAttributeValue savedServiceItemAttributeValue = serviceItemAttributeValueRepository.save(serviceItemAttributeValue);
        return serviceItemAttributeValueMapper.toDto(savedServiceItemAttributeValue);
    }

    // Add other methods as needed...
}
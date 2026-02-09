package io.quickledger.services.serviceitem;

import io.quickledger.dto.serviceitem.ServiceItemDto;
import io.quickledger.dto.serviceitem.ServiceItemAttributeValueDto;
import io.quickledger.entities.*;
import io.quickledger.entities.serviceitem.ServiceItem;
import io.quickledger.entities.serviceitem.ServiceItemAttributeDefinition;
import io.quickledger.entities.serviceitem.ServiceItemAttributeValue;
import io.quickledger.mappers.serviceitem.ServiceItemMapper;
import io.quickledger.mappers.serviceitem.ServiceItemAttributeDefinitionMapper;
import io.quickledger.mappers.serviceitem.ServiceItemAttributeValueMapper;
import io.quickledger.repositories.serviceitem.ServiceItemRepository;
import io.quickledger.repositories.serviceitem.ServiceItemAttributeValueRepository;
import io.quickledger.services.asset.AssetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceItemService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceItemService.class);

    private final ServiceItemRepository serviceItemRepository;
    private final ServiceItemAttributeValueRepository serviceItemAttributeValueRepository;
    private final ServiceItemMapper serviceItemMapper;
    private final ServiceItemAttributeValueMapper serviceItemAttributeValueMapper;
    private final ServiceItemAttributeDefinitionMapper serviceItemAttributeDefinitionMapper;
    private final ServiceItemAttributeDefinitionService serviceItemAttributeDefinitionService;
    private final ServiceItemAttributeValueService serviceItemAttributeValueService;
    private final AssetService assetService;

    public ServiceItemService(ServiceItemRepository serviceItemRepository, ServiceItemAttributeValueRepository serviceItemAttributeValueRepository,
                              ServiceItemMapper serviceItemMapper, ServiceItemAttributeValueMapper serviceItemAttributeValueMapper, ServiceItemAttributeDefinitionMapper serviceItemAttributeDefinitionMapper, ServiceItemAttributeDefinitionService serviceItemAttributeDefinitionService, ServiceItemAttributeValueService serviceItemAttributeValueService, AssetService assetService) {
        this.serviceItemRepository = serviceItemRepository;
        this.serviceItemAttributeValueRepository = serviceItemAttributeValueRepository;
        this.serviceItemMapper = serviceItemMapper;
        this.serviceItemAttributeValueMapper = serviceItemAttributeValueMapper;
        this.serviceItemAttributeDefinitionMapper = serviceItemAttributeDefinitionMapper;
        this.serviceItemAttributeDefinitionService = serviceItemAttributeDefinitionService;
        this.serviceItemAttributeValueService = serviceItemAttributeValueService;
        this.assetService = assetService;
    }

    @Transactional
    public ServiceItemDto saveServiceItem(ServiceItemDto serviceItemDto, Company company) {
        ServiceItem serviceItem = serviceItemMapper.toEntity(serviceItemDto);
        serviceItem.setCompany(company);

        //Set asset if presented
//        Optional<Asset> assetOptional = assetService.getAssetEntityByIdAndCompanyId(serviceItemDto.getAssetId(), company.getId());
//        assetOptional.ifPresent(serviceItem::setAsset);

        ServiceItem savedServiceItem = serviceItemRepository.save(serviceItem);

        List<ServiceItemAttributeValueDto> savedServiceItemAttributeValues = Collections.emptyList();
        if (serviceItemDto.getServiceItemAttributeValues() != null && !serviceItemDto.getServiceItemAttributeValues().isEmpty()) {
            savedServiceItemAttributeValues = serviceItemDto.getServiceItemAttributeValues().stream()
                    .map(serviceItemAttributeValueDto -> {
                        ServiceItemAttributeValue serviceItemAttributeValue = serviceItemAttributeValueMapper.toEntity(serviceItemAttributeValueDto);

                        ServiceItemAttributeDefinition savedSIADefinition = serviceItemAttributeDefinitionService.findOrCreateByName(serviceItemAttributeValueDto.getDefinition().getName());
                        serviceItemAttributeValue.setDefinition(savedSIADefinition);
                        serviceItemAttributeValue.setServiceItem(savedServiceItem);
                        return serviceItemAttributeValueMapper.toDto(serviceItemAttributeValueRepository.save(serviceItemAttributeValue));
                    })
                    .collect(Collectors.toList());
        }

        ServiceItemDto responseDto = serviceItemMapper.toDto(savedServiceItem);
        responseDto.setServiceItemAttributeValues(savedServiceItemAttributeValues);

        return responseDto;
    }

    public List<ServiceItemDto> getAllCompanyServiceItems(Long companyId) {
        return serviceItemRepository.findByCompanyId(companyId)
                .stream()
                .map(serviceItemMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<ServiceItemDto> getServiceItemByIdAndCompanyId(Long serviceItemId, Long companyId) {
        return serviceItemRepository.findByIdAndCompanyId(serviceItemId, companyId).map(serviceItemMapper::toDto);
    }

    public void deleteServiceItem(Long companyId, Long serviceItemId) {
        // Implementation here...
    }
    // Add other methods as needed...
}
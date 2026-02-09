package io.quickledger.services.serviceitem;

import io.quickledger.entities.serviceitem.ServiceItemAttributeDefinition;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.quickledger.repositories.serviceitem.ServiceItemAttributeDefinitionRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceItemAttributeDefinitionService {
    private static final Logger logger = LoggerFactory.getLogger(ServiceItemAttributeDefinitionService.class);
    private final ServiceItemAttributeDefinitionRepository serviceItemAttributeDefinitionRepository;

    public ServiceItemAttributeDefinitionService(ServiceItemAttributeDefinitionRepository serviceItemAttributeDefinitionRepository) {
        this.serviceItemAttributeDefinitionRepository = serviceItemAttributeDefinitionRepository;
    }

    @Transactional
    public ServiceItemAttributeDefinition save(ServiceItemAttributeDefinition serviceItemAttributeDefinition) {
        logger.debug("==> Saving service item attribute definition: {}", serviceItemAttributeDefinition.toString());
        return serviceItemAttributeDefinitionRepository.save(serviceItemAttributeDefinition);
    }

    public ServiceItemAttributeDefinition findByName(String name) {
        return serviceItemAttributeDefinitionRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("ServiceItemAttributeDefinition with name " + name + " not found"));
    }

    @Transactional
    public ServiceItemAttributeDefinition findOrCreateByName(String name) {
        return serviceItemAttributeDefinitionRepository.findByName(name)
                .orElseGet(() -> {
                    ServiceItemAttributeDefinition newDefinition = new ServiceItemAttributeDefinition();
                    newDefinition.setName(name);
                    return serviceItemAttributeDefinitionRepository.save(newDefinition);
                });
    }
    // Add other methods as needed...
}
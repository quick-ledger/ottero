package io.quickledger.repositories.serviceitem;

import io.quickledger.entities.serviceitem.ServiceItemAttributeDefinition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceItemAttributeDefinitionRepository extends CrudRepository<ServiceItemAttributeDefinition, Long> {
    Optional<ServiceItemAttributeDefinition> findByName(String name);
}


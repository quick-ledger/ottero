package io.quickledger.repositories.serviceitem;

import io.quickledger.entities.serviceitem.ServiceItemAttributeValue;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceItemAttributeValueRepository extends CrudRepository<ServiceItemAttributeValue, Long> {
}
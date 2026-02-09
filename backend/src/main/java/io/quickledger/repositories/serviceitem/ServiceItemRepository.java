package io.quickledger.repositories.serviceitem;

import io.quickledger.entities.serviceitem.ServiceItem;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceItemRepository extends CrudRepository<ServiceItem, Long> {
    List<ServiceItem> findByCompanyId(Long companyId);

    Optional<ServiceItem> findByIdAndCompanyId(Long serviceItemId, Long companyId);
}

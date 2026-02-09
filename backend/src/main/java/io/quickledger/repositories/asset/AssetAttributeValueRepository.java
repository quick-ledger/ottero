package io.quickledger.repositories.asset;

import io.quickledger.entities.asset.AssetAttributeValue;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetAttributeValueRepository extends CrudRepository<AssetAttributeValue, Long> {
    //count by definition
    public Long countByDefinitionId(Long definitionId);
}
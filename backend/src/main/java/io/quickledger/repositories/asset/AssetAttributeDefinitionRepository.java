package io.quickledger.repositories.asset;


import io.quickledger.entities.asset.AssetAttributeDefinition;
import io.quickledger.entities.asset.AssetGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetAttributeDefinitionRepository extends CrudRepository<AssetAttributeDefinition, Long> {
    Optional<AssetAttributeDefinition> findByName(String name);

    List<AssetAttributeDefinition> findByAssetGroup(AssetGroup assetGroup);
}


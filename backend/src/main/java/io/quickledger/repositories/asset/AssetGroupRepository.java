package io.quickledger.repositories.asset;

import io.quickledger.entities.asset.AssetGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetGroupRepository extends CrudRepository<AssetGroup, Long> {
}
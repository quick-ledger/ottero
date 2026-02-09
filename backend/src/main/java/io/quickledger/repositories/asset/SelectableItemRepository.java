package io.quickledger.repositories.asset;

import io.quickledger.entities.asset.Selectable;
import io.quickledger.entities.asset.SelectableItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelectableItemRepository extends CrudRepository<SelectableItem, Long> {
    //Optional<AssetAttributeDefinition> findByName(String name);
}

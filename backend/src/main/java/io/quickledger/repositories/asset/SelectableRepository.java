package io.quickledger.repositories.asset;

import io.quickledger.entities.asset.Selectable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelectableRepository extends CrudRepository<Selectable, Long> {
    //Optional<AssetAttributeDefinition> findByName(String name);
    List<Selectable> findAllByType(Selectable.SelectableType type);
}

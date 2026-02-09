package io.quickledger.repositories.asset;

import io.quickledger.entities.asset.Asset;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
/*
    * For the security reasons we should never directly access asset by AssetId alone, it should always come with companyId
 */
public interface AssetRepository extends CrudRepository<Asset, Long>{
    //    Optional<Asset> findById(Long id);
    //    void deleteByAssetId(Long assetId);

    List<Asset> findAllByCompanyId(Long companyId);

    Optional<Asset> findByIdAndCompanyId(Long id, Long companyId);

    void deleteByIdAndCompanyId(Long id, Long companyId);
}

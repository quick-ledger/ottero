package io.quickledger.services.asset;

import io.quickledger.dto.asset.AssetAttributeValueDto;
import io.quickledger.entities.asset.Asset;
import io.quickledger.entities.asset.AssetAttributeValue;
import io.quickledger.entities.Company;
import io.quickledger.mappers.asset.AssetMapper;
import io.quickledger.mappers.asset.AssetAttributeDefinitionMapper;
import io.quickledger.mappers.asset.AssetAttributeValueMapper;
import io.quickledger.repositories.asset.AssetRepository;
import io.quickledger.repositories.asset.AssetAttributeValueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.quickledger.dto.asset.AssetDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AssetService {

    private static final Logger logger = LoggerFactory.getLogger(AssetService.class);

    private final AssetRepository assetRepository;
    private final AssetAttributeValueRepository assetAttributeValueRepository;
    private final AssetMapper assetMapper;
    private final AssetAttributeValueMapper assetAttributeValueMapper;
    private final AssetAttributeDefinitionMapper assetAttributeDefinitionMapper;
    private final AssetDefinitionService assetAttributeDefinitionService;
    private final AssetAttributeValueService assetAttributeValueService;

    public AssetService(AssetRepository assetRepository, AssetAttributeValueRepository assetAttributeValueRepository,
                        AssetMapper assetMapper, AssetAttributeValueMapper assetAttributeValueMapper, AssetAttributeDefinitionMapper assetAttributeDefinitionMapper, AssetDefinitionService assetAttributeDefinitionService, AssetAttributeValueService assetAttributeValueService) {
        this.assetRepository = assetRepository;
        this.assetAttributeValueRepository = assetAttributeValueRepository;
        this.assetMapper = assetMapper;
        this.assetAttributeValueMapper = assetAttributeValueMapper;
        this.assetAttributeDefinitionMapper = assetAttributeDefinitionMapper;
        this.assetAttributeDefinitionService = assetAttributeDefinitionService;
        this.assetAttributeValueService = assetAttributeValueService;
    }

    public List<AssetDto> getAllCompanyAssets(Long companyId) {
        List<Asset> assets = assetRepository.findAllByCompanyId(companyId);
        logger.debug("==> Assets fetched from repository: {}", assets.toString());
        List<AssetDto> assetDtos = assetMapper.toAssetDtoList(assets);

        return assetDtos;
    }

    //MBH changed to optional
    public Optional<AssetDto> getAssetDtoByIdAndCompanyId(Long id, Long companyId) {
        return assetRepository.findByIdAndCompanyId(id, companyId).map(assetMapper::toDto);
    }

    public Optional<Asset> getAssetEntityByIdAndCompanyId(Long id, Long companyId) {
        return assetRepository.findByIdAndCompanyId(id, companyId);
    }



    @Transactional
    public AssetDto saveAsset(AssetDto assetDto, Company company) {
        Asset asset = assetMapper.toEntity(assetDto);
        asset.setCompany(company);
        Asset savedAsset = assetRepository.save(asset);


        for (AssetAttributeValueDto valueDto : assetDto.getValueDTOs()) {
            //TODO convert
            AssetAttributeValue value = new AssetAttributeValue();
            //TODO take this out of the loop and use saveAll()
            assetAttributeValueRepository.save(value);
        }

        return assetDto;//TODO fix this response;
    }

//        if (assetDto.getValueDTOs() != null && !assetDto.getAssetAttributeValues().isEmpty()) {
//             savedAssetAttributeValues = assetDto.getAssetAttributeValues().stream()
//                .map(assetAttributeValueDto -> {
//                    AssetAttributeValue assetAttributeValue = assetAttributeValueMapper.toEntity(assetAttributeValueDto);
//
//                    // TODO mani and RG to check what shoould be the expected behaviour, I think it's good to reuse the definitions.
//                    AssetAttributeDefinition savedATDefinition = assetAttributeDefinitionService.findOrCreateByName(assetAttributeValue.getDefinition().getName());
////                    AssetAttributeDefinition savedATDefinition = assetAttributeDefinitionService.save(assetAttributeValue.getDefinition());
//                    assetAttributeValue.setDefinition(savedATDefinition);
//                    assetAttributeValue.setAsset(savedAsset);
//                    return assetAttributeValueRepository.save(assetAttributeValue);
//                })
//                .collect(Collectors.toList());
//}
//        savedAsset.setAssetAttributeValues(savedAssetAttributeValues);
//
//        AssetDto responseDto = assetMapper.toDto(savedAsset);
//        responseDto.setAssetAttributeValues(savedAssetAttributeValues.stream()
//                .map(assetAttributeValueMapper::toDto)
//                .collect(Collectors.toList()));
//


    /*
        * INFO: This method is used to save an Asset entity along with its attribute values in a single transaction
        *  This will work only if if AssetAttributeValue I set @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) for AssetAttributeDefinition
        * But seems like that's not a good practice to have cascade all for ManyToOne relation as it might cause issues in future.

    @Transactional
    public AssetDto saveAsset(AssetDto assetDto, Company company) {
        // Convert DTO to Entity and set the company
        Asset asset = assetMapper.toEntity(assetDto);
        asset.setCompany(company);
        // Save the Asset entity
        Asset savedAsset = assetRepository.save(asset);

        // Initialize an empty list for asset attribute values
        List<AssetAttributeValue> savedAssetAttributeValues = Collections.emptyList();

        // Check if asset attribute values are provided and not empty
        if (assetDto.getAssetAttributeValues() != null && !assetDto.getAssetAttributeValues().isEmpty()) {
            savedAssetAttributeValues = assetDto.getAssetAttributeValues().stream()
                    .map(assetAttributeValueDto -> {
                        AssetAttributeValue assetAttributeValue = assetAttributeValueMapper.toEntity(assetAttributeValueDto);
                        assetAttributeValue.setAsset(savedAsset);  // Link the saved asset to this attribute value
                        return assetAttributeValueRepository.save(assetAttributeValue);  // Save the attribute value
                    })
                    .collect(Collectors.toList());
        }

        // Refresh the list of attribute values in the saved asset
        savedAsset.setAssetAttributeValues(savedAssetAttributeValues);

        // Map the saved asset and its attributes back to a DTO
        AssetDto responseDto = assetMapper.toDto(savedAsset);
        responseDto.setAssetAttributeValues(savedAssetAttributeValues.stream()
                .map(assetAttributeValueMapper::toDto)
                .collect(Collectors.toList()));

        return responseDto;
    }

*/


    public void deleteAsset(Long id, Long companyId) {
        assetRepository.deleteByIdAndCompanyId(id, companyId);
    }
}
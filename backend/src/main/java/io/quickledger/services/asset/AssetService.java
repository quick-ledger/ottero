package io.quickledger.services.asset;

import io.quickledger.dto.asset.AssetAttributeValueDto;
import io.quickledger.entities.asset.Asset;
import io.quickledger.entities.asset.AssetAttributeValue;
import io.quickledger.entities.asset.AssetGroup;
import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.mappers.asset.AssetMapper;
import io.quickledger.mappers.asset.AssetAttributeDefinitionMapper;
import io.quickledger.mappers.asset.AssetAttributeValueMapper;
import io.quickledger.repositories.asset.AssetRepository;
import io.quickledger.repositories.asset.AssetAttributeValueRepository;
import io.quickledger.repositories.asset.AssetGroupRepository;
import io.quickledger.services.PlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final AssetGroupRepository assetGroupRepository;
    private final AssetMapper assetMapper;
    private final AssetAttributeValueMapper assetAttributeValueMapper;
    private final AssetAttributeDefinitionMapper assetAttributeDefinitionMapper;
    private final AssetDefinitionService assetAttributeDefinitionService;
    private final AssetAttributeValueService assetAttributeValueService;
    private final PlanService planService;

    public AssetService(AssetRepository assetRepository, AssetAttributeValueRepository assetAttributeValueRepository,
                        AssetGroupRepository assetGroupRepository,
                        AssetMapper assetMapper, AssetAttributeValueMapper assetAttributeValueMapper,
                        AssetAttributeDefinitionMapper assetAttributeDefinitionMapper,
                        AssetDefinitionService assetAttributeDefinitionService,
                        AssetAttributeValueService assetAttributeValueService,
                        PlanService planService) {
        this.assetRepository = assetRepository;
        this.assetAttributeValueRepository = assetAttributeValueRepository;
        this.assetGroupRepository = assetGroupRepository;
        this.assetMapper = assetMapper;
        this.assetAttributeValueMapper = assetAttributeValueMapper;
        this.assetAttributeDefinitionMapper = assetAttributeDefinitionMapper;
        this.assetAttributeDefinitionService = assetAttributeDefinitionService;
        this.assetAttributeValueService = assetAttributeValueService;
        this.planService = planService;
    }

    private void validateAccess(User user) {
        planService.requireFeature(user, PlanService.Feature.ASSET_MANAGEMENT);
    }

    @Transactional(readOnly = true)
    public List<AssetDto> getAllCompanyAssets(Long companyId, User user) {
        validateAccess(user);
        List<Asset> assets = assetRepository.findAllByCompanyId(companyId);
        logger.debug("==> Assets fetched from repository: {}", assets.toString());
        List<AssetDto> assetDtos = assetMapper.toAssetDtoList(assets);

        return assetDtos;
    }

    @Transactional(readOnly = true)
    public Page<AssetDto> getAllCompanyAssets(Long companyId, Pageable pageable, User user) {
        validateAccess(user);
        return assetRepository.findAllByCompanyId(companyId, pageable)
                .map(assetMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<AssetDto> getAssetDtoByIdAndCompanyId(Long id, Long companyId, User user) {
        validateAccess(user);
        return assetRepository.findByIdAndCompanyId(id, companyId).map(assetMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<Asset> getAssetEntityByIdAndCompanyId(Long id, Long companyId, User user) {
        validateAccess(user);
        return assetRepository.findByIdAndCompanyId(id, companyId);
    }



    @Transactional
    public AssetDto saveAsset(AssetDto assetDto, Company company, User user) {
        validateAccess(user);
        Asset asset = assetMapper.toEntity(assetDto);
        asset.setCompany(company);

        // Fetch existing AssetGroup from database if assetGroupId is provided
        if (assetDto.getAssetGroupId() != null) {
            AssetGroup assetGroup = assetGroupRepository.findById(assetDto.getAssetGroupId())
                    .orElse(null);
            asset.setAssetGroup(assetGroup);
        } else {
            asset.setAssetGroup(null);
        }

        Asset savedAsset = assetRepository.save(asset);

        if (assetDto.getValueDTOs() != null) {
            for (AssetAttributeValueDto valueDto : assetDto.getValueDTOs()) {
                //TODO convert
                AssetAttributeValue value = new AssetAttributeValue();
                //TODO take this out of the loop and use saveAll()
                assetAttributeValueRepository.save(value);
            }
        }

        return assetMapper.toDto(savedAsset);
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


    @Transactional
    public void deleteAsset(Long id, Long companyId, User user) {
        validateAccess(user);
        assetRepository.deleteByIdAndCompanyId(id, companyId);
    }
}
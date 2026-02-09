package io.quickledger.services.asset;

import io.quickledger.dto.asset.AssetAttributeValueDto;
import io.quickledger.entities.asset.AssetAttributeValue;
import io.quickledger.mappers.asset.AssetAttributeValueMapper;
import io.quickledger.services.asset.validator.CompositeSaveValidator;
import io.quickledger.services.asset.validator.values.MandatoryValuesValidator;
import org.springframework.stereotype.Service;
import io.quickledger.repositories.asset.AssetAttributeValueRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssetAttributeValueService {

    private final AssetAttributeValueRepository assetAttributeValueRepository;
    private final AssetAttributeValueMapper assetAttributeValueMapper;

    public AssetAttributeValueService(AssetAttributeValueRepository assetAttributeValueRepository, AssetAttributeValueMapper assetAttributeValueMapper) {
        this.assetAttributeValueRepository = assetAttributeValueRepository;
        this.assetAttributeValueMapper = assetAttributeValueMapper;
    }

    public AssetAttributeValueDto saveAttributeValue(AssetAttributeValueDto assetAttributeValueDto) {
        AssetAttributeValue assetAttributeValue = assetAttributeValueMapper.toEntity(assetAttributeValueDto);
        AssetAttributeValue savedAssetAttributeValue = assetAttributeValueRepository.save(assetAttributeValue);
        return assetAttributeValueMapper.toDto(savedAssetAttributeValue);
    }

    public AssetAttributeValue saveAttributeValue(AssetAttributeValue assetAttributeValue) {
        return assetAttributeValueRepository.save(assetAttributeValue);
    }

    public AssetAttributeValueDto saveAttributeValues(List<AssetAttributeValueDto> assetAttributeValueDtos) {

        CompositeSaveValidator<AssetAttributeValue> validator = new CompositeSaveValidator<>();
        validator.addValidator(new MandatoryValuesValidator());

        List<AssetAttributeValue> values = new ArrayList<>();
        assetAttributeValueDtos.forEach(assetAttributeValueDto -> {
            AssetAttributeValue assetAttributeValue = assetAttributeValueMapper.toEntity(assetAttributeValueDto);
            values.add(assetAttributeValue);
        });

        assetAttributeValueRepository.saveAll(values);
        return null; //TODO
    }
}
package io.quickledger.services.asset;


import io.quickledger.dto.asset.AssetAttributeDefinitionDto;
import io.quickledger.dto.asset.AssetAttributeValueDto;
import io.quickledger.entities.asset.AssetAttributeDefinition;
import io.quickledger.entities.asset.AssetGroup;
import io.quickledger.exception.BusinessException;
import io.quickledger.repositories.asset.AssetAttributeValueRepository;
import io.quickledger.services.asset.validator.*;
import io.quickledger.services.asset.validator.def.DefaultValueAndTypeSaveValidator;
import io.quickledger.services.asset.validator.def.NotRequiredToRequiredUpdateValidator;
import io.quickledger.services.asset.validator.def.TypeCheckUpdateValidator;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.quickledger.repositories.asset.AssetAttributeDefinitionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssetDefinitionService {
    private static final Logger logger = LoggerFactory.getLogger(AssetDefinitionService.class);
    private final AssetAttributeDefinitionRepository assetAttributeDefinitionRepository;
    private final AssetAttributeValueRepository assetAttributeValueRepository;
    //private final ObservationAutoConfiguration.OnlyMetricsConfiguration onlyMetricsConfiguration;

    public AssetDefinitionService(AssetAttributeDefinitionRepository assetAttributeDefinitionRepository, AssetAttributeValueRepository assetAttributeValueRepository) {
        this.assetAttributeDefinitionRepository = assetAttributeDefinitionRepository;
        this.assetAttributeValueRepository = assetAttributeValueRepository;
    }

    @Transactional
    public AssetAttributeDefinition save( AssetAttributeDefinition assetAttributeDefinition , Long companyId) {

        logger.debug("==> Saving asset attribute definition: {}", assetAttributeDefinition.toString());

        CompositeSaveValidator<AssetAttributeDefinition> validator = new CompositeSaveValidator<>();
        validator.addValidator(new DefaultValueAndTypeSaveValidator());
        validator.addValidator(new DefaultValueAndTypeSaveValidator());

        if (!validator.validate(assetAttributeDefinition).isValid()) {
            throw new IllegalArgumentException("Invalid asset attribute definition: " + assetAttributeDefinition.getName());
        }
        return assetAttributeDefinitionRepository.save(assetAttributeDefinition);
    }

    public AssetAttributeDefinition save(AssetAttributeDefinition def) {
        return assetAttributeDefinitionRepository.save(def);
    }


    @Transactional
    public void update(AssetAttributeDefinitionDto incomingDefDto, Long companyId) {
        AssetAttributeDefinition incomingDef = new AssetAttributeDefinition();
        //TODO convert
        AssetAttributeDefinition existingDef = assetAttributeDefinitionRepository.findById(incomingDef.getId()).get();


        CompositeUpdateValidator<AssetAttributeDefinition, AssetAttributeDefinition> validator = new CompositeUpdateValidator<>();
        validator.addValidator(new NotRequiredToRequiredUpdateValidator()).
                    addValidator(new TypeCheckUpdateValidator());

        //count of usage. some updates can only be done if there is no asset instance
        Long usageCount = assetAttributeValueRepository.countByDefinitionId(incomingDef.getId());
        ValidationResult r = validator.validate(existingDef, incomingDef, usageCount);

        if (!r.isValid()) throw new BusinessException(r.getMessage());


        //example1: change disabled and default value and type from string to number.
        //example2: change regex
        //example3: change unit from weight to length
        //example4: this is a problem - default value is 4 with unit of G change to 5 with unit of KG. we need default of a unit item.

        //NEW FEATURE: empty out the values of a field attribute so that a new value can be entered with a different type.

        //update def
        assetAttributeDefinitionRepository.save(incomingDef);

        // update value
        assetAttributeValueRepository.findById(incomingDef.getId()).ifPresent(assetAttributeValue -> {
            assetAttributeValue.setDefinition(incomingDef);
            assetAttributeValueRepository.save(assetAttributeValue);
        });
    }

    public AssetAttributeValueDto saveAttributeValue(AssetAttributeValueDto assetAttributeValueDto) {
//        AssetAttributeValue assetAttributeValue = assetAttributeValueMapper.toEntity(assetAttributeValueDto);
//        AssetAttributeValue savedAssetAttributeValue = assetAttributeValueRepository.save(assetAttributeValue);
//        return assetAttributeValueMapper.toDto(savedAssetAttributeValue);
        return null;
    }

    AssetAttributeDefinition findById(Long id) {
        return assetAttributeDefinitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AssetAttributeDefinition with id " + id + " not found"));
    }


    public AssetAttributeDefinition findByName(String name) {
        return assetAttributeDefinitionRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("AssetAttributeDefinition with name " + name + " not found"));
    }


    List<AssetAttributeDefinition> findByAssetGroup(AssetGroup assetGroup) {
        return assetAttributeDefinitionRepository.findByAssetGroup(assetGroup);
    }

    @Transactional
    public AssetAttributeDefinition findOrCreateByName(String name) {
        return assetAttributeDefinitionRepository.findByName(name)
                .orElseGet(() -> {
                    AssetAttributeDefinition newDefinition = new AssetAttributeDefinition();
                    newDefinition.setName(name);
                    return assetAttributeDefinitionRepository.save(newDefinition);
                });
    }

    public void save(List<AssetAttributeDefinitionDto> assetDefDTOs, Long companyId) {
    }
    // Add other methods as needed...
}

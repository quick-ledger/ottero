package io.quickledger.services;

import io.quickledger.dto.SequenceConfigDTO;
import io.quickledger.entities.SequenceConfig;
import io.quickledger.mappers.SequenceConfigMapper;
import io.quickledger.repositories.SequenceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SequenceConfigService {

    private final SequenceConfigRepository sequenceConfigRepository;
    private final SequenceConfigMapper sequenceConfigMapper;

    @Autowired
    public SequenceConfigService(SequenceConfigRepository sequenceConfigRepository, SequenceConfigMapper sequenceConfigMapper) {
        this.sequenceConfigRepository = sequenceConfigRepository;
        this.sequenceConfigMapper = sequenceConfigMapper;
    }

//    public List<SequenceConfigDTO> createOrUpdateSequenceConfigs(Long companyId, List<SequenceConfigDTO> sequenceConfigDTOs) {
//        return sequenceConfigDTOs.stream()
//                .map(dto -> {
//                    SequenceConfig sequenceConfig = sequenceConfigMapper.toEntity(dto);
//                    sequenceConfig.setCompanyId(companyId);
//                    sequenceConfig = sequenceConfigRepository.save(sequenceConfig);
//                    return sequenceConfigMapper.toDto(sequenceConfig);
//                })
//                .collect(Collectors.toList());
//    }

    public SequenceConfigDTO createOrUpdateSequenceConfig(Long companyId, SequenceConfigDTO sequenceConfigDTO) {
        SequenceConfig sequenceConfig = sequenceConfigMapper.toEntity(sequenceConfigDTO);
        sequenceConfig = sequenceConfigRepository.save(sequenceConfig);
        return sequenceConfigMapper.toDto(sequenceConfig);
    }

    // In SequenceConfigService.java
    public List<SequenceConfigDTO> findAllByCompanyId(Long companyId) {
        return sequenceConfigRepository.findByCompanyId(companyId)
                .map(list -> list.stream().map(sequenceConfigMapper::toDto).collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    public SequenceConfigDTO findByIdAndCompanyId(Long id, Long companyId) {
        Optional<SequenceConfig> sequenceConfig = sequenceConfigRepository.findByIdAndCompanyId(id, companyId);
        return sequenceConfig.map(sequenceConfigMapper::toDto).orElse(null);
    }

    public void deleteByIdAndCompanyId(Long id, Long companyId) {
        sequenceConfigRepository.deleteByIdAndCompanyId(id, companyId);
    }

    private SequenceConfigDTO convertToDto(SequenceConfig sequenceConfig) {
        return sequenceConfigMapper.toDto(sequenceConfig);
    }

    public SequenceConfigDTO findByTypeAndCompanyId(SequenceConfig.EntityType type, Long companyId) {
        Optional<SequenceConfig> sequenceConfig = sequenceConfigRepository.findByEntityTypeAndCompanyId(type, companyId);
        return sequenceConfig.map(sequenceConfigMapper::toDto).orElse(null);
    }
}
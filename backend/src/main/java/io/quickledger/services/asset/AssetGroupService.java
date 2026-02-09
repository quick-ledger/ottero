package io.quickledger.services.asset;

import io.quickledger.dto.asset.AssetGroupDto;
import io.quickledger.entities.asset.AssetGroup;
import io.quickledger.entities.Company;
import io.quickledger.mappers.asset.AssetGroupMapper;
import io.quickledger.repositories.asset.AssetGroupRepository;
import io.quickledger.services.CompanyService;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AssetGroupService {

    private static final Logger logger = LoggerFactory.getLogger(AssetGroupService.class);

    private final AssetGroupRepository assetGroupRepository;
    private final AssetGroupMapper assetGroupMapper;
    private final CompanyService companyService;

    public AssetGroupService(AssetGroupRepository assetGroupRepository, AssetGroupMapper assetGroupMapper, CompanyService companyService) {
        this.assetGroupRepository = assetGroupRepository;
        this.assetGroupMapper = assetGroupMapper;
        this.companyService = companyService;
    }
    public List<AssetGroupDto> getAllAssetGroups() {
        return StreamSupport.stream(assetGroupRepository.findAll().spliterator(), false)
                .map(assetGroupMapper::toDto)
                .collect(Collectors.toList());
    }

    public AssetGroupDto getAssetGroupById(Long id) {
        AssetGroup assetGroup = assetGroupRepository.findById(id).orElseThrow(() -> new RuntimeException("AssetGroup not found"));
        return assetGroupMapper.toDto(assetGroup);
    }

    public AssetGroupDto createAssetGroup(AssetGroupDto assetGroupDto, Long companyId) {
        AssetGroup assetGroup = assetGroupMapper.toEntity(assetGroupDto);
        Optional<Company> company = companyService.getCompanyById(companyId);
        assetGroup.setCompany(company.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found")));
        assetGroup = assetGroupRepository.save(assetGroup);
        return assetGroupMapper.toDto(assetGroup);
    }

    public void deleteAssetGroup(Long id) {
        assetGroupRepository.deleteById(id);
    }
}
package io.quickledger.controllers.asset;

import io.quickledger.dto.asset.AssetGroupDto;
import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.asset.AssetGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/asset-groups")
public class AssetGroupController {

    private static final Logger logger = LoggerFactory.getLogger(AssetGroupController.class);
    private final AssetGroupService assetGroupService;

    public AssetGroupController(AssetGroupService assetGroupService) {
        this.assetGroupService = assetGroupService;
    }

    @GetMapping
    public List<AssetGroupDto> getAllAssetGroups(@UserIdAuth User user, @PathVariable Long companyId) {
        return assetGroupService.getAllAssetGroups();
    }

    @GetMapping("/{id}")
    public AssetGroupDto getAssetGroupById(@UserIdAuth User user, @PathVariable Long id, @PathVariable Long companyId) {
        return assetGroupService.getAssetGroupById(id);
    }

    @PostMapping
    public AssetGroupDto createAssetGroup(@UserIdAuth User user, @RequestBody AssetGroupDto assetGroupDto, @PathVariable Long companyId) {
        return assetGroupService.createAssetGroup(assetGroupDto, companyId);
    }

    @DeleteMapping("/{id}")
    public void deleteAssetGroup(@UserIdAuth User user, @PathVariable Long id, @PathVariable Long companyId) {
        assetGroupService.deleteAssetGroup(id);
    }
}
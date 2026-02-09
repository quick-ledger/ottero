package io.quickledger.controllers.asset;

import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.asset.AssetService;
import io.quickledger.services.CompanyService;
import io.quickledger.services.UserCompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.quickledger.dto.asset.AssetDto;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/assets")
public class AssetController {

    private static final Logger logger = LoggerFactory.getLogger(AssetController.class);

    private final AssetService assetService;
    private final CompanyService companyService;
    private final UserCompanyService userCompanyService;

    public AssetController(AssetService assetService, CompanyService companyService, UserCompanyService userCompanyService) {
        this.assetService = assetService;
        this.companyService = companyService;
        this.userCompanyService = userCompanyService;
    }

    @GetMapping
    public ResponseEntity<List<AssetDto>> getAllCompanyAssets(@PathVariable Long companyId, @UserIdAuth User user) {
        return ResponseEntity.ok(assetService.getAllCompanyAssets(companyId));
    }

    @GetMapping("/{assetId}")
    public ResponseEntity<AssetDto> getAssetById(@PathVariable Long companyId, @PathVariable Long assetId, @UserIdAuth User user) {
        return assetService.getAssetDtoByIdAndCompanyId(assetId, companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AssetDto> createAsset(@PathVariable Long companyId, @RequestBody AssetDto assetDto, @UserIdAuth User user) {
        assetDto.setCompanyId(companyId);
        return ResponseEntity.ok(assetService.saveAsset(assetDto, companyService.findCompanyById(companyId)));
    }

    @PutMapping("/{assetId}")
    public ResponseEntity<AssetDto> updateAsset(@PathVariable Long companyId, @PathVariable Long assetId, @RequestBody AssetDto assetDto, @UserIdAuth User user) {
        assetDto.setId(assetId);
        assetDto.setCompanyId(companyId);
        return ResponseEntity.ok(assetService.saveAsset(assetDto, companyService.findCompanyById(companyId)));
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long companyId, @PathVariable Long assetId, @UserIdAuth User user) {
        assetService.deleteAsset(companyId, assetId);
        return ResponseEntity.noContent().build();
    }
}
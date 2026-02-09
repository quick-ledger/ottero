package io.quickledger.controllers.asset;

import io.quickledger.dto.asset.AssetAttributeDefinitionDto;
import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.asset.AssetDefinitionService;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/asset-definitions")
public class AssetDefController {

    private static final Logger logger = LoggerFactory.getLogger(AssetDefController.class);
    private final AssetDefinitionService assetDefService;

    public AssetDefController(AssetDefinitionService assetDefService) {
        this.assetDefService = assetDefService;
    }

    //add is applied to a list of AssetAttributeDefinitionDto
    @PostMapping
    public Response.ResponseBuilder createAssetDef(@UserIdAuth User user, @RequestBody List<AssetAttributeDefinitionDto> assetDefDTOs, @PathVariable Long companyId) {
        assetDefService.save(assetDefDTOs, companyId);
        return Response.ok();
    }

    //we allow update one field at a time only
    @PutMapping
    public Response.ResponseBuilder updateAssetDef(@UserIdAuth User user, @RequestBody AssetAttributeDefinitionDto assetDefDto, @PathVariable Long companyId) {
        assetDefService.update(assetDefDto, companyId);
        return Response.ok();
    }
}
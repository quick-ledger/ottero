package io.quickledger.controllers.asset;

import io.quickledger.dto.asset.AssetAttributeValueDto;
import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.asset.AssetAttributeValueService;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/asset-values")
public class AssetValuesController {

    private static final Logger logger = LoggerFactory.getLogger(AssetValuesController.class);
    private final AssetAttributeValueService assetAttributeValueService;

    public AssetValuesController(AssetAttributeValueService assetValueService) {
        this.assetAttributeValueService = assetValueService;
    }

    @PostMapping
    public Response.ResponseBuilder createAssetValues(@UserIdAuth User user, @RequestBody List<AssetAttributeValueDto> assetDTOs, @PathVariable Long companyId) {
        return Response.ok();
    }

}
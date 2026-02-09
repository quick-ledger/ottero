package io.quickledger.controllers;

import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.serviceitem.ServiceItemService;
import io.quickledger.services.CompanyService;
import io.quickledger.services.UserCompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.quickledger.dto.serviceitem.ServiceItemDto;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/service_items")
public class ServiceItemController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceItemController.class);

    private final ServiceItemService serviceItemService;
    private final CompanyService companyService;
    private final UserCompanyService userCompanyService;

    public ServiceItemController(ServiceItemService serviceItemService, CompanyService companyService, UserCompanyService userCompanyService) {
        this.serviceItemService = serviceItemService;
        this.companyService = companyService;
        this.userCompanyService = userCompanyService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceItemDto>> getAllCompanyServiceItems(@PathVariable Long companyId, @UserIdAuth User user) {
        // Implement this method in ServiceItemService
        return ResponseEntity.ok(serviceItemService.getAllCompanyServiceItems(companyId));
    }

    @GetMapping("/{serviceItemId}")
    public ResponseEntity<ServiceItemDto> getServiceItemById(@PathVariable Long companyId, @PathVariable Long serviceItemId, @UserIdAuth User user) {
        return serviceItemService.getServiceItemByIdAndCompanyId(serviceItemId, companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ServiceItemDto> createServiceItem(@PathVariable Long companyId, @RequestBody ServiceItemDto serviceItemDto, @UserIdAuth User user) {
        serviceItemDto.setCompanyId(companyId);
        return ResponseEntity.ok(serviceItemService.saveServiceItem(serviceItemDto, companyService.findCompanyById(companyId)));
    }

//    @PutMapping("/{serviceItemId}")
//    public ResponseEntity<ServiceItemDto> updateServiceItem(@PathVariable Long companyId, @PathVariable Long serviceItemId, @RequestBody ServiceItemDto serviceItemDto, @UserIdAuth User user) {
//        serviceItemDto.setId(serviceItemId);
//        serviceItemDto.setCompanyId(companyId);
//        return ResponseEntity.ok(serviceItemService.saveServiceItem(serviceItemDto, companyService.findCompanyById(companyId)));
//    }

    @DeleteMapping("/{serviceItemId}")
    public ResponseEntity<Void> deleteServiceItem(@PathVariable Long companyId, @PathVariable Long serviceItemId, @UserIdAuth User user) {
        // Implement this method in ServiceItemService
        serviceItemService.deleteServiceItem(companyId, serviceItemId);
        return ResponseEntity.noContent().build();
    }
}
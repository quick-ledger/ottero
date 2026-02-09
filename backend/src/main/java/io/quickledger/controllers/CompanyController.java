package io.quickledger.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quickledger.dto.CompanyDto;
import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.mappers.CompanyMapper;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.CompanyService;
import io.quickledger.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;
    // private final KeycloakAdminService keycloakAdminService;
    private final UserService userService;
    // private final UserCompanyService userCompanyService;

    @Autowired
    public CompanyController(CompanyService companyService, UserService userService) {
        this.companyService = companyService;
        // this.keycloakAdminService = keycloakAdminService;
        // this.userCompanyService = userCompanyService;
        this.userService = userService;
    }

    @GetMapping
    public Iterable<CompanyDto> getAllCompanies(@UserIdAuth final User user) {
        logger.debug("===> Getting list of all companies for user , {}", user.getId());
        Iterable<Company> allCompanies = companyService.findCompaniesByUser(user.getId());
        return StreamSupport.stream(allCompanies.spliterator(), false)
                .map(CompanyMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{companyId}")
    public CompanyDto getCompanyById(@PathVariable Long companyId, @UserIdAuth final User user) {
        logger.debug("===> Getting Company Id:" + companyId);
        Company company = companyService.findCompanyById(companyId);

        // Make sure the user has access to this company
        List<Company> userCompanies = companyService.findCompaniesByUser(user.getId());
        if (!userCompanies.contains(company)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have access to this company");
        }

        return CompanyMapper.INSTANCE.toDto(company);

    }

    // removed userId from the method signature as it does not exist in other ones
    // in this controller. RG
    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> createCompany(@RequestBody CompanyDto companyDto, @UserIdAuth final User user)
            throws JsonProcessingException {
        logger.debug("===> creating new company" + companyDto.toString());
        Company company;
        // Create a Map with field names and values for mandatory mandatoryFields
        Map<String, String> mandatoryFields = new HashMap<>();
        mandatoryFields.put("name", companyDto.getName());
        // mandatoryFields.put("userId", company.toString());

        // Check for missing mandatory mandatoryFields
        List<String> missingFields = mandatoryFields.entrySet().stream()
                .filter(entry -> entry.getValue() == null || entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("Missing mandatory field(s): " + String.join(", ", missingFields));
        }
        validateCompanyName(companyDto.getName());

        company = CompanyMapper.INSTANCE.toEntity(companyDto);
        company = companyService.createCompanyWithUser(company, user, company.getName());

        // Create a Map to hold the response data
        Map<String, Object> response = new HashMap<>();
        response.put("id", company.getId());
        response.put("name", company.getName());
        // Convert the response Map to JSON
        String jsonResponse = new ObjectMapper().writeValueAsString(response);
        return ResponseEntity.status(HttpStatus.OK).body(jsonResponse);
    }

    private void validateCompanyName(String name) {
        if (name.length() > 200) {
            throw new IllegalArgumentException("Company name cannot exceed 200 characters");
        }

        if (!name.matches("[a-zA-Z0-9 &-]*")) {
            throw new IllegalArgumentException("Company name contains invalid characters");
        }

        if (name.matches(".*['\";].*")) {
            throw new IllegalArgumentException("Company name contains SQL special characters");
        }
    }

    @PostMapping(value = "/{companyId}/logo", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadCompanyLogo(@PathVariable Long companyId,
            @RequestParam("file") MultipartFile file,
            @UserIdAuth final User user) {
        logger.debug("Uploading logo for company: {}", companyId);

        // Validate file is an image
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("File must be an image (PNG, JPEG, GIF, etc.)");
        }

        // Validate file size (max 5MB for logo)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Logo file size must not exceed 5MB");
        }

        Optional<Company> optionalCompany = companyService.getCompanyById(companyId);

        // Check if user has access to this company
        List<Company> userCompanies = companyService.findCompaniesByUser(user.getId());
        if (optionalCompany.isEmpty() || !userCompanies.contains(optionalCompany.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User does not have access to this company");
        }

        try {
            Company company = optionalCompany.get();
            byte[] fileBytes = file.getBytes();
            company.setImage(fileBytes);
            company.setLogoContentType(contentType);
            companyService.saveCompany(company);

            logger.info("Logo uploaded successfully for company: {}, size: {} bytes, type: {}",
                    companyId, file.getSize(), contentType);
            return ResponseEntity.ok("Logo uploaded successfully");
        } catch (IOException e) {
            logger.error("Error uploading logo for company: {}", companyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading logo");
        }
    }

    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getCompanyLogo(@PathVariable Long id) {
        logger.debug("Fetching logo for company: {}", id);

        Optional<Company> optionalCompany = companyService.getCompanyById(id);

        if (optionalCompany.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Company company = optionalCompany.get();
        byte[] image = company.getImage();

        if (image == null || image.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        // Set content type if available
        if (company.getLogoContentType() != null) {
            headers.set(HttpHeaders.CONTENT_TYPE, company.getLogoContentType());
        } else {
            // Default to image/png if not set
            headers.set(HttpHeaders.CONTENT_TYPE, "image/png");
        }

        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{companyId}/logo")
    public ResponseEntity<String> deleteCompanyLogo(@PathVariable Long companyId,
            @UserIdAuth final User user) {
        logger.debug("Deleting logo for company: {}", companyId);

        Optional<Company> optionalCompany = companyService.getCompanyById(companyId);

        // Check if user has access to this company
        List<Company> userCompanies = companyService.findCompaniesByUser(user.getId());
        if (optionalCompany.isEmpty() || !userCompanies.contains(optionalCompany.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User does not have access to this company");
        }

        Company company = optionalCompany.get();
        company.setImage(null);
        company.setLogoContentType(null);
        companyService.saveCompany(company);

        logger.info("Logo deleted successfully for company: {}", companyId);
        return ResponseEntity.ok("Logo deleted successfully");
    }

    // Legacy endpoint for backwards compatibility
    @PatchMapping(value = "/{companyId}/images", consumes = "multipart/form-data")
    public ResponseEntity<String> addCompanyImages(@PathVariable Long companyId,
            @RequestParam("file") MultipartFile file, @UserIdAuth final User user) {
        // Redirect to new logo endpoint
        return uploadCompanyLogo(companyId, file, user);
    }

    // Legacy endpoint for backwards compatibility
    @GetMapping("/{id}/images")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        // Redirect to new logo endpoint
        return getCompanyLogo(id);
    }

    @PutMapping("/{companyId}")
    public CompanyDto updateCompany(@PathVariable Long companyId, @RequestBody CompanyDto companyDto) {
        Company company = CompanyMapper.INSTANCE.toEntity(companyDto);
        company.setId(companyId); // Ensure the ID is set to the one we want to update
        company = companyService.saveCompany(company);
        return CompanyMapper.INSTANCE.toDto(company);
    }

    @DeleteMapping("/{companyId}")
    public void deleteCompany(@PathVariable Long companyId, @UserIdAuth final User user) {
        companyService.deleteCompany(companyId, user);
    }

    @GetMapping("/{companyId}/template-config")
    public ResponseEntity<String> getTemplateConfig(@PathVariable Long companyId, @UserIdAuth final User user) {
        logger.debug("Fetching template config for company: {}", companyId);

        // Check if user has access to this company
        List<Company> userCompanies = companyService.findCompaniesByUser(user.getId());
        Optional<Company> optionalCompany = companyService.getCompanyById(companyId);

        if (optionalCompany.isEmpty() || !userCompanies.contains(optionalCompany.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{}");
        }

        Company company = optionalCompany.get();
        String templateConfig = company.getTemplateConfig();

        if (templateConfig == null || templateConfig.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
        }

        return ResponseEntity.ok(templateConfig);
    }

    @PostMapping("/{companyId}/template-config")
    public ResponseEntity<String> saveTemplateConfig(@PathVariable Long companyId,
            @RequestBody String templateConfigJson,
            @UserIdAuth final User user) {
        logger.debug("Saving template config for company: {}", companyId);

        // Check if user has access to this company
        List<Company> userCompanies = companyService.findCompaniesByUser(user.getId());
        Optional<Company> optionalCompany = companyService.getCompanyById(companyId);

        if (optionalCompany.isEmpty() || !userCompanies.contains(optionalCompany.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this company");
        }

        Company company = optionalCompany.get();
        company.setTemplateConfig(templateConfigJson);
        companyService.saveCompany(company);

        logger.info("Template config saved successfully for company: {}", companyId);
        return ResponseEntity.ok("Template config saved successfully");
    }

}
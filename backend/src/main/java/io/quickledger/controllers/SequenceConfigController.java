package io.quickledger.controllers;

import io.quickledger.dto.SequenceConfigDTO;
import io.quickledger.entities.SequenceConfig;
import io.quickledger.services.SequenceConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/sequence-configs")
public class SequenceConfigController {

    private final SequenceConfigService sequenceConfigService;
    private static final Logger logger = LoggerFactory.getLogger(SequenceConfigService.class);

    @Autowired
    public SequenceConfigController(SequenceConfigService sequenceConfigService) {
        this.sequenceConfigService = sequenceConfigService;
    }

    //this is added when company is created not by user
//    @PostMapping
//    public ResponseEntity<SequenceConfigDTO> createSequenceConfigs(@PathVariable Long companyId, @RequestBody SequenceConfigDTO sequenceConfigDTO) {
//        SequenceConfigDTO createdSequenceConfig = sequenceConfigService.createOrUpdateSequenceConfig(companyId, sequenceConfigDTO);
//        return ResponseEntity.ok(createdSequenceConfig);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<SequenceConfigDTO> getSequenceConfigById(@PathVariable Long companyId, @PathVariable Long id) {
        SequenceConfigDTO sequenceConfigDTO = sequenceConfigService.findByIdAndCompanyId(id, companyId);
        return ResponseEntity.ok(sequenceConfigDTO);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<SequenceConfigDTO> getSequenceConfigByType(@PathVariable Long companyId, @PathVariable String type) {
        SequenceConfigDTO sequenceConfigDTO = sequenceConfigService.findByTypeAndCompanyId(SequenceConfig.EntityType.valueOf(type), companyId);
        return ResponseEntity.ok(sequenceConfigDTO);
    }

    //TODO should we allow update?! this is tricky. maybe as long as there is no quote generated?? maybe prefix/postfix but not the seq???
    @PutMapping
    public ResponseEntity<SequenceConfigDTO> updateSequenceConfig(@PathVariable Long companyId, @RequestBody SequenceConfigDTO sequenceConfigDTO) {
        //TODO check user access
        logger.info("Updating sequence config for company: {}", sequenceConfigDTO);
        SequenceConfigDTO updatedSequenceConfig = sequenceConfigService.createOrUpdateSequenceConfig(companyId, sequenceConfigDTO);
        return ResponseEntity.ok(updatedSequenceConfig);
    }

    /*
        we decided not to allow delete.
     */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logger.error("DataIntegrityViolationException: {}", e.getMessage());
        String message = "Unable to process the request due to a conflict. Each company can have only 1 INVOICE and 1 QUOTE config. Error: " +  e.getMessage();
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        logger.error("Internal Server Error: {}", e.getMessage());
        return new ResponseEntity<>("An unexpected error occurred. Please try again later. Error:" +  e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

}
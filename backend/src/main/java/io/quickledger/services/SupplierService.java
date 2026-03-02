package io.quickledger.services;

import io.quickledger.dto.supplier.SupplierDto;
import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.entities.supplier.Supplier;
import io.quickledger.mappers.supplier.SupplierMapper;
import io.quickledger.repositories.supplier.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final PlanService planService;

    public SupplierService(
            SupplierRepository supplierRepository,
            SupplierMapper supplierMapper,
            PlanService planService) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
        this.planService = planService;
    }

    private void validateAccess(User user) {
        planService.requireFeature(user, PlanService.Feature.INVENTORY_MANAGEMENT);
    }

    @Transactional
    public SupplierDto createOrUpdate(SupplierDto dto, Long companyId, User user) {
        validateAccess(user);

        Supplier supplier;

        if (dto.getId() != null) {
            supplier = supplierRepository.findByIdAndCompanyId(dto.getId(), companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
            supplierMapper.updateEntityFromDto(dto, supplier);
        } else {
            supplier = supplierMapper.toEntity(dto);
            supplier.setCompany(new Company(companyId));
            if (supplier.getIsActive() == null) {
                supplier.setIsActive(true);
            }
        }

        supplier = supplierRepository.save(supplier);
        logger.info("Saved supplier: {} for company: {}", supplier.getId(), companyId);
        return supplierMapper.toDto(supplier);
    }

    @Transactional(readOnly = true)
    public Page<SupplierDto> getAllSuppliers(Long companyId, Pageable pageable, User user) {
        validateAccess(user);
        return supplierRepository.findAllByCompanyIdOrderByCreatedDateDesc(companyId, pageable)
                .map(supplierMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<SupplierDto> getActiveSuppliers(Long companyId, User user) {
        validateAccess(user);
        return supplierRepository.findByCompanyIdAndIsActiveTrueOrderByNameAsc(companyId)
                .stream()
                .map(supplierMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierDto getSupplierById(Long id, Long companyId, User user) {
        validateAccess(user);
        Supplier supplier = supplierRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
        return supplierMapper.toDto(supplier);
    }

    @Transactional
    public void deleteSupplier(Long id, Long companyId, User user) {
        validateAccess(user);
        Supplier supplier = supplierRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

        // TODO: Check if supplier has any purchase orders before deleting
        // For now, just soft-delete by setting isActive to false
        supplier.setIsActive(false);
        supplierRepository.save(supplier);
        logger.info("Deactivated supplier: {} for company: {}", id, companyId);
    }

    @Transactional(readOnly = true)
    public Page<SupplierDto> searchSuppliers(Long companyId, String searchTerm, Pageable pageable, User user) {
        validateAccess(user);
        return supplierRepository.searchSuppliers(companyId, searchTerm, pageable)
                .map(supplierMapper::toDto);
    }
}

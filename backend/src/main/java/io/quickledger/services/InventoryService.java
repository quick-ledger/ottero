package io.quickledger.services;

import io.quickledger.dto.inventory.InventoryDashboardDto;
import io.quickledger.dto.inventory.LowStockAlertDto;
import io.quickledger.dto.inventory.StockMovementDto;
import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.entities.inventory.StockMovement;
import io.quickledger.entities.inventory.StockMovement.MovementType;
import io.quickledger.entities.inventory.StockMovement.ReferenceType;
import io.quickledger.entities.invoice.Invoice;
import io.quickledger.entities.invoice.InvoiceItem;
import io.quickledger.entities.product.ProductItem;
import io.quickledger.mappers.inventory.StockMovementMapper;
import io.quickledger.repositories.inventory.StockMovementRepository;
import io.quickledger.repositories.product.ProductItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final StockMovementRepository stockMovementRepository;
    private final ProductItemRepository productItemRepository;
    private final StockMovementMapper stockMovementMapper;
    private final PlanService planService;

    public InventoryService(
            StockMovementRepository stockMovementRepository,
            ProductItemRepository productItemRepository,
            StockMovementMapper stockMovementMapper,
            PlanService planService) {
        this.stockMovementRepository = stockMovementRepository;
        this.productItemRepository = productItemRepository;
        this.stockMovementMapper = stockMovementMapper;
        this.planService = planService;
    }

    private void validateAccess(User user) {
        planService.requireFeature(user, PlanService.Feature.INVENTORY_MANAGEMENT);
    }

    /**
     * Record a stock movement and update product quantity.
     * Core method for all inventory changes.
     */
    @Transactional
    public StockMovement recordStockMovement(
            ProductItem product,
            MovementType movementType,
            int quantityChange,
            ReferenceType referenceType,
            Long referenceId,
            String referenceNumber,
            String notes,
            Company company) {

        int quantityBefore = product.getQuantityOnHand() != null ? product.getQuantityOnHand() : 0;
        int quantityAfter = quantityBefore + quantityChange;

        // Allow negative stock (backorders) but log warning
        if (quantityAfter < 0) {
            logger.warn("Stock for product {} going negative: {} -> {}",
                    product.getId(), quantityBefore, quantityAfter);
        }

        // Create movement record
        StockMovement movement = new StockMovement();
        movement.setCompany(company);
        movement.setProductItem(product);
        movement.setMovementType(movementType);
        movement.setQuantityChange(quantityChange);
        movement.setQuantityBefore(quantityBefore);
        movement.setQuantityAfter(quantityAfter);
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setReferenceNumber(referenceNumber);
        movement.setNotes(notes);

        stockMovementRepository.save(movement);

        // Update product quantity
        product.setQuantityOnHand(quantityAfter);
        productItemRepository.save(product);

        logger.info("Recorded stock movement: {} {} units for product {}, {} -> {}",
                movementType, quantityChange, product.getId(), quantityBefore, quantityAfter);

        return movement;
    }

    /**
     * Process stock deduction when invoice is sent.
     * Called from InvoiceService when invoice status changes to SENT.
     */
    @Transactional
    public void processInvoiceStockDeduction(Invoice invoice) {
        if (invoice.getInvoiceItems() == null || invoice.getInvoiceItems().isEmpty()) {
            return;
        }

        for (InvoiceItem item : invoice.getInvoiceItems()) {
            if (item.getProductItem() != null &&
                    Boolean.TRUE.equals(item.getProductItem().getTrackInventory())) {

                recordStockMovement(
                        item.getProductItem(),
                        MovementType.SALE,
                        -item.getQuantity(), // Negative for outgoing
                        ReferenceType.INVOICE,
                        invoice.getId(),
                        invoice.getInvoiceNumber(),
                        "Invoice sale",
                        invoice.getCompany()
                );
            }
        }
    }

    /**
     * Get inventory dashboard data.
     */
    @Transactional(readOnly = true)
    public InventoryDashboardDto getInventoryDashboard(Long companyId, User user) {
        validateAccess(user);

        InventoryDashboardDto dashboard = new InventoryDashboardDto();

        dashboard.setTotalProducts(productItemRepository.countTrackedProducts(companyId));

        List<ProductItem> lowStockProducts = productItemRepository.findLowStockProducts(companyId);
        dashboard.setLowStockCount(lowStockProducts.size());

        List<ProductItem> outOfStockProducts = productItemRepository.findOutOfStockProducts(companyId);
        dashboard.setOutOfStockCount(outOfStockProducts.size());

        BigDecimal totalValue = productItemRepository.calculateTotalInventoryValue(companyId);
        dashboard.setTotalInventoryValue(totalValue != null ? totalValue : BigDecimal.ZERO);

        dashboard.setLowStockAlerts(lowStockProducts.stream()
                .map(this::toLowStockAlertDto)
                .collect(Collectors.toList()));

        return dashboard;
    }

    /**
     * Get low stock alerts.
     */
    @Transactional(readOnly = true)
    public List<LowStockAlertDto> getLowStockAlerts(Long companyId, User user) {
        validateAccess(user);

        return productItemRepository.findLowStockProducts(companyId).stream()
                .map(this::toLowStockAlertDto)
                .collect(Collectors.toList());
    }

    /**
     * Manual stock adjustment.
     */
    @Transactional
    public StockMovementDto adjustStock(Long productId, int newQuantity, String reason, Long companyId, User user) {
        validateAccess(user);

        ProductItem product = productItemRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (!Boolean.TRUE.equals(product.getTrackInventory())) {
            throw new IllegalStateException("Product does not have inventory tracking enabled");
        }

        int currentQty = product.getQuantityOnHand() != null ? product.getQuantityOnHand() : 0;
        int change = newQuantity - currentQty;

        StockMovement movement = recordStockMovement(
                product,
                MovementType.ADJUSTMENT,
                change,
                ReferenceType.MANUAL,
                null,
                null,
                reason,
                product.getCompany()
        );

        return stockMovementMapper.toDto(movement);
    }

    /**
     * Get stock movements for a product.
     */
    @Transactional(readOnly = true)
    public Page<StockMovementDto> getProductMovements(Long productId, Long companyId, Pageable pageable, User user) {
        validateAccess(user);

        return stockMovementRepository
                .findByCompanyIdAndProductItemIdOrderByCreatedDateDesc(companyId, productId, pageable)
                .map(stockMovementMapper::toDto);
    }

    /**
     * Get all stock movements for company.
     */
    @Transactional(readOnly = true)
    public Page<StockMovementDto> getAllMovements(Long companyId, Pageable pageable, User user) {
        validateAccess(user);

        return stockMovementRepository
                .findAllByCompanyIdOrderByCreatedDateDesc(companyId, pageable)
                .map(stockMovementMapper::toDto);
    }

    private LowStockAlertDto toLowStockAlertDto(ProductItem product) {
        LowStockAlertDto dto = new LowStockAlertDto();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setProductCode(product.getCode());
        dto.setQuantityOnHand(product.getQuantityOnHand());
        dto.setReorderPoint(product.getReorderPoint());
        dto.setReorderQuantity(product.getReorderQuantity());
        return dto;
    }
}

package io.quickledger.services;

import io.quickledger.dto.purchaseorder.PurchaseOrderDto;
import io.quickledger.dto.purchaseorder.PurchaseOrderItemDto;
import io.quickledger.dto.purchaseorder.ReceiveItemsDto;
import io.quickledger.entities.Company;
import io.quickledger.entities.SequenceConfig;
import io.quickledger.entities.User;
import io.quickledger.entities.inventory.StockMovement.MovementType;
import io.quickledger.entities.inventory.StockMovement.ReferenceType;
import io.quickledger.entities.product.ProductItem;
import io.quickledger.entities.purchaseorder.PurchaseOrder;
import io.quickledger.entities.purchaseorder.PurchaseOrder.PurchaseOrderStatus;
import io.quickledger.entities.purchaseorder.PurchaseOrderItem;
import io.quickledger.entities.supplier.Supplier;
import io.quickledger.mappers.purchaseorder.PurchaseOrderMapper;
import io.quickledger.repositories.SequenceConfigRepository;
import io.quickledger.repositories.product.ProductItemRepository;
import io.quickledger.repositories.purchaseorder.PurchaseOrderRepository;
import io.quickledger.repositories.supplier.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderService.class);

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductItemRepository productItemRepository;
    private final SequenceConfigRepository sequenceConfigRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final InventoryService inventoryService;
    private final PlanService planService;

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            ProductItemRepository productItemRepository,
            SequenceConfigRepository sequenceConfigRepository,
            PurchaseOrderMapper purchaseOrderMapper,
            InventoryService inventoryService,
            PlanService planService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productItemRepository = productItemRepository;
        this.sequenceConfigRepository = sequenceConfigRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.inventoryService = inventoryService;
        this.planService = planService;
    }

    private void validateAccess(User user) {
        planService.requireFeature(user, PlanService.Feature.INVENTORY_MANAGEMENT);
    }

    @Transactional
    public PurchaseOrderDto createOrUpdate(PurchaseOrderDto dto, Long companyId, User user) {
        validateAccess(user);

        PurchaseOrder po;

        if (dto.getId() != null) {
            po = purchaseOrderRepository.findByIdAndCompanyId(dto.getId(), companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

            if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
                throw new IllegalStateException("Only DRAFT purchase orders can be modified");
            }

            purchaseOrderMapper.updateEntityFromDto(dto, po);
        } else {
            po = purchaseOrderMapper.toEntity(dto);
            po.setCompany(new Company(companyId));
            po.setPoNumber(generatePOSequence(companyId));
            po.setStatus(PurchaseOrderStatus.DRAFT);

            if (po.getOrderDate() == null) {
                po.setOrderDate(LocalDate.now());
            }
        }

        // Set supplier
        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findByIdAndCompanyId(dto.getSupplierId(), companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
            po.setSupplier(supplier);
        }

        // Handle items
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            // Clear existing items for update
            if (dto.getId() != null) {
                po.getItems().clear();
            }

            for (PurchaseOrderItemDto itemDto : dto.getItems()) {
                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setPurchaseOrder(po);

                ProductItem product = productItemRepository.findById(itemDto.getProductItemId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Product not found: " + itemDto.getProductItemId()));
                item.setProductItem(product);

                item.setItemOrder(itemDto.getItemOrder());
                item.setQuantityOrdered(itemDto.getQuantityOrdered());
                item.setQuantityReceived(0);
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setGst(itemDto.getGst());
                item.calculateTotal();

                po.getItems().add(item);
            }
        }

        // Calculate totals
        calculateTotals(po);

        po = purchaseOrderRepository.save(po);
        logger.info("Saved purchase order: {} for company: {}", po.getPoNumber(), companyId);

        return purchaseOrderMapper.toDto(po);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderDto> getAllPurchaseOrders(Long companyId, Pageable pageable, User user) {
        validateAccess(user);
        return purchaseOrderRepository.findAllByCompanyIdOrderByCreatedDateDesc(companyId, pageable)
                .map(purchaseOrderMapper::toDto);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDto getPurchaseOrderById(Long id, Long companyId, User user) {
        validateAccess(user);
        PurchaseOrder po = purchaseOrderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));
        return purchaseOrderMapper.toDto(po);
    }

    @Transactional
    public void deletePurchaseOrder(Long id, Long companyId, User user) {
        validateAccess(user);
        PurchaseOrder po = purchaseOrderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT purchase orders can be deleted");
        }

        purchaseOrderRepository.delete(po);
        logger.info("Deleted purchase order: {} for company: {}", id, companyId);
    }

    @Transactional
    public PurchaseOrderDto sendPurchaseOrder(Long id, Long companyId, User user) {
        validateAccess(user);
        PurchaseOrder po = purchaseOrderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT purchase orders can be sent");
        }

        if (po.getItems() == null || po.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot send purchase order with no items");
        }

        po.setStatus(PurchaseOrderStatus.SENT);
        po = purchaseOrderRepository.save(po);

        logger.info("Sent purchase order: {} for company: {}", po.getPoNumber(), companyId);
        return purchaseOrderMapper.toDto(po);
    }

    @Transactional
    public PurchaseOrderDto receiveItems(ReceiveItemsDto dto, Long companyId, User user) {
        validateAccess(user);

        PurchaseOrder po = purchaseOrderRepository.findByIdAndCompanyIdForUpdate(dto.getPurchaseOrderId(), companyId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (po.getStatus() == PurchaseOrderStatus.DRAFT || po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot receive items for " + po.getStatus() + " purchase orders");
        }

        for (ReceiveItemsDto.ReceiveItemDto receiveItem : dto.getItems()) {
            PurchaseOrderItem poItem = po.getItems().stream()
                    .filter(item -> item.getId().equals(receiveItem.getPurchaseOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Purchase order item not found: " + receiveItem.getPurchaseOrderItemId()));

            int newReceived = poItem.getQuantityReceived() + receiveItem.getQuantityReceived();
            if (newReceived > poItem.getQuantityOrdered()) {
                throw new IllegalStateException("Cannot receive more than ordered for item: " + poItem.getId());
            }

            poItem.setQuantityReceived(newReceived);

            // Update inventory
            if (Boolean.TRUE.equals(poItem.getProductItem().getTrackInventory())) {
                inventoryService.recordStockMovement(
                        poItem.getProductItem(),
                        MovementType.PURCHASE,
                        receiveItem.getQuantityReceived(), // Positive for incoming
                        ReferenceType.PURCHASE_ORDER,
                        po.getId(),
                        po.getPoNumber(),
                        dto.getNotes(),
                        po.getCompany()
                );
            }
        }

        // Update PO status
        updatePOStatus(po);

        po = purchaseOrderRepository.save(po);
        logger.info("Received items for purchase order: {} for company: {}", po.getPoNumber(), companyId);

        return purchaseOrderMapper.toDto(po);
    }

    private void updatePOStatus(PurchaseOrder po) {
        boolean allReceived = true;
        boolean anyReceived = false;

        for (PurchaseOrderItem item : po.getItems()) {
            if (item.getQuantityReceived() > 0) anyReceived = true;
            if (item.getQuantityReceived() < item.getQuantityOrdered()) allReceived = false;
        }

        if (allReceived) {
            po.setStatus(PurchaseOrderStatus.RECEIVED);
        } else if (anyReceived) {
            po.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }
    }

    private void calculateTotals(PurchaseOrder po) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;

        if (po.getItems() != null) {
            for (PurchaseOrderItem item : po.getItems()) {
                item.calculateTotal();
                if (item.getTotal() != null) {
                    total = total.add(item.getTotal());
                }
                if (item.getGst() != null && item.getUnitPrice() != null) {
                    BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantityOrdered()));
                    BigDecimal gstAmount = subtotal.multiply(item.getGst())
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    totalGst = totalGst.add(gstAmount);
                }
            }
        }

        po.setTotalAmount(total);
        po.setGst(totalGst);
    }

    private String generatePOSequence(Long companyId) {
        Optional<SequenceConfig> sequenceConfigOptional = sequenceConfigRepository
                .findByEntityTypeAndCompanyIdForUpdate(SequenceConfig.EntityType.PURCHASE_ORDER, companyId);

        SequenceConfig sequenceConfig;
        if (!sequenceConfigOptional.isPresent()) {
            // Create default sequence config for purchase orders
            sequenceConfig = new SequenceConfig();
            sequenceConfig.setEntityType(SequenceConfig.EntityType.PURCHASE_ORDER);
            sequenceConfig.setCompanyId(companyId);
            sequenceConfig.setPrefix("PO-");
            sequenceConfig.setPostfix("");
            sequenceConfig.setCurrentNumber(0);
            sequenceConfig.setNumberPadding(4);
            sequenceConfig = sequenceConfigRepository.save(sequenceConfig);
        } else {
            sequenceConfig = sequenceConfigOptional.get();
        }

        int newSequence = sequenceConfig.getCurrentNumber() + 1;

        String newPONumber = io.quickledger.utils.SequenceNumberFormatter.format(
                sequenceConfig.getPrefix(),
                newSequence,
                sequenceConfig.getPostfix(),
                sequenceConfig.getNumberPadding(),
                LocalDate.now());

        sequenceConfig.setCurrentNumber(newSequence);
        sequenceConfigRepository.save(sequenceConfig);

        return newPONumber;
    }
}

package io.quickledger.entities.inventory;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Company;
import io.quickledger.entities.product.ProductItem;
import jakarta.persistence.*;

/**
 * Tracks all inventory changes for audit trail.
 * Every stock change (sale, purchase, adjustment) creates a movement record.
 */
@Entity
@Table(name = "stock_movements",
        indexes = {
                @Index(name = "idx_stock_movement_product", columnList = "product_item_id"),
                @Index(name = "idx_stock_movement_company", columnList = "company_id"),
                @Index(name = "idx_stock_movement_reference", columnList = "reference_type, reference_id")
        })
public class StockMovement extends BaseEntity {

    public enum MovementType {
        SALE,           // Stock out from invoice
        PURCHASE,       // Stock in from purchase order
        ADJUSTMENT,     // Manual adjustment
        TRANSFER,       // Between locations (future)
        RETURN          // Customer return
    }

    public enum ReferenceType {
        INVOICE,
        PURCHASE_ORDER,
        MANUAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_item_id", nullable = false)
    private ProductItem productItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 50)
    private MovementType movementType;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange; // Positive for in, negative for out

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 50)
    private ReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId; // Invoice ID, PO ID, etc.

    @Column(name = "reference_number", length = 100)
    private String referenceNumber; // INV-001, PO-001, etc.

    @Column(name = "notes", length = 1000)
    private String notes;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public Integer getQuantityBefore() {
        return quantityBefore;
    }

    public void setQuantityBefore(Integer quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public Integer getQuantityAfter() {
        return quantityAfter;
    }

    public void setQuantityAfter(Integer quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(ReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", movementType=" + movementType +
                ", quantityChange=" + quantityChange +
                ", referenceNumber='" + referenceNumber + '\'' +
                '}';
    }
}

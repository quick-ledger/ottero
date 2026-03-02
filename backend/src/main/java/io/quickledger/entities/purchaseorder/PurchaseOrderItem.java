package io.quickledger.entities.purchaseorder;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.product.ProductItem;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Line item on a purchase order.
 * Tracks ordered quantity and received quantity for partial receiving.
 */
@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_item_id", nullable = false)
    private ProductItem productItem;

    @Column(name = "item_order", nullable = false)
    private int itemOrder;

    @Column(name = "quantity_ordered", nullable = false)
    private int quantityOrdered;

    @Column(name = "quantity_received", nullable = false, columnDefinition = "integer default 0")
    private int quantityReceived = 0;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "gst", precision = 5, scale = 2, columnDefinition = "decimal(5,2) default 10.00")
    private BigDecimal gst = new BigDecimal("10.00");

    @Column(name = "total", precision = 19, scale = 2)
    private BigDecimal total;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public int getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(int itemOrder) {
        this.itemOrder = itemOrder;
    }

    public int getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(int quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public int getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(int quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getGst() {
        return gst;
    }

    public void setGst(BigDecimal gst) {
        this.gst = gst;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    /**
     * Calculate total including GST
     */
    public void calculateTotal() {
        if (unitPrice != null && quantityOrdered > 0) {
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantityOrdered));
            if (gst != null && gst.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal gstAmount = subtotal.multiply(gst).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                this.total = subtotal.add(gstAmount);
            } else {
                this.total = subtotal;
            }
        }
    }

    /**
     * Get remaining quantity to receive
     */
    public int getRemainingQuantity() {
        return quantityOrdered - quantityReceived;
    }

    @Override
    public String toString() {
        return "PurchaseOrderItem{" +
                "id=" + id +
                ", itemOrder=" + itemOrder +
                ", quantityOrdered=" + quantityOrdered +
                ", quantityReceived=" + quantityReceived +
                ", unitPrice=" + unitPrice +
                '}';
    }
}

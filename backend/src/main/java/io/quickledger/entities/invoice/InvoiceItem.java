package io.quickledger.entities.invoice;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.product.ProductItem;
import io.quickledger.entities.serviceitem.ServiceItem;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items") // This names the table as "invoice_items" in the database
public class InvoiceItem extends BaseEntity {
    // default constructor
    public InvoiceItem() {
    }

    // Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", referencedColumnName = "id")
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "service_item_id", referencedColumnName = "id")
    private ServiceItem serviceItem;

    @ManyToOne
    @JoinColumn(name = "product_item_id", referencedColumnName = "id")
    private ProductItem productItem;

    @Column(name = "item_description", length = 4000, nullable = true)
    private String itemDescription;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "item_order", nullable = false)
    private int itemOrder;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "total", nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(name = "gst", nullable = false, precision = 5, scale = 2, columnDefinition = "decimal(5,2) default 10.00")
    private BigDecimal gst;

    @Column(name = "code", length = 500, nullable = true) // item code (QBR or barcode or ...)
    private String code;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public ServiceItem getServiceItem() {
        return serviceItem;
    }

    public void setServiceItem(ServiceItem serviceItem) {
        this.serviceItem = serviceItem;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getGst() {
        return gst;
    }

    public void setGst(BigDecimal gst) {
        this.gst = gst;
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
}

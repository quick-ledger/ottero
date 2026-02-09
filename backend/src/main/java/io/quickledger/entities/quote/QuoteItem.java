package io.quickledger.entities.quote;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.product.ProductItem;
import io.quickledger.entities.serviceitem.ServiceItem;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "quote_items") // This names the table as "quote_items" in the database
public class QuoteItem extends BaseEntity {
    //default constructor
    public QuoteItem() {
    }

    //Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quote_id", referencedColumnName = "id", nullable = false)
    private Quote quote;

    @ManyToOne
    @JoinColumn(name = "service_item_id", referencedColumnName = "id")
    private ServiceItem serviceItem;

    @ManyToOne
    @JoinColumn(name = "product_item_id", referencedColumnName = "id")
    private ProductItem productItem;

    @Column(name = "item_description", length = 4000)
    private String itemDescription;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    //MBH: order in quoteitem, must be filled by GUI
    @Column(name = "item_order", nullable = false)
    private int itemOrder;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;


    @Column(name="gst", nullable = false, precision = 5, scale = 2, columnDefinition = "decimal(5,2) default 10.00")
    private BigDecimal gst;

    @Column(name = "total", nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(name = "code", length = 100)
    private String code;



    //Getters and Setters


    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
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

    public int getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(int itemOrder) {
        this.itemOrder = itemOrder;
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

    //toString method
    @Override
    public String toString() {
        return "QuoteItem{" +
                "id=" + id + '\'' +
                ", serviceItem=" + serviceItem + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", quantity=" + quantity + '\'' +
                ", price=" + price + '\'' +
                ", total=" + total + '\'' +
                '}';
    }
}

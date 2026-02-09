package io.quickledger.entities;
import jakarta.persistence.*;


@Entity
@Table(name = "warehouses") // This names the table as "warehouses" in the database
public class Warehouse extends BaseEntity{

    public Warehouse() {
    }
    //Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = false)
    private Company company;

    @Column(name = "warehouse_name", length = 500, nullable = true)
    private String warehouseName;
    @Column(name = "warehouse_address", length = 500, nullable = true)
    private String warehouseAddress;

    @Column(name = "warehouse_description", length = 1000, nullable = false)
    private String warehouseDescription;

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

    public String getWarehouseDescription() {
        return warehouseDescription;
    }

    public void setWarehouseDescription(String warehouseDescription) {
        this.warehouseDescription = warehouseDescription;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getWarehouseAddress() {
        return warehouseAddress;
    }

    public void setWarehouseAddress(String warehouseAddress) {
        this.warehouseAddress = warehouseAddress;
    }

    @Override
    public String toString() {
        return "Warehouse{" +
                "id=" + id +
                ", company=" + company +
                ", warehouseName='" + warehouseName + '\'' +
                ", warehouseAddress='" + warehouseAddress + '\'' +
                ", warehouseDescription='" + warehouseDescription + '\'' +
                ", baseEntity=" + super.toString() + '\'' +
                '}';
    }




}

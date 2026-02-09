package io.quickledger.entities;
import jakarta.persistence.*;
@Entity
@Table(name = "locations") // This names the table as "locations" in the database
public class Location extends BaseEntity{

    //Default constructor
    public Location() {
    }

    //Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "warehouseId")
    private Warehouse warehouse;
    @Column(name = "locationName", length = 500, nullable = true) // General name or identifier
    private String locationName;
    @Column(name = "description", length = 1000, nullable = true)
    private String description;

    @Column(name = "section", length = 500, nullable = true) // Larger area within the warehouse
    private String section;

    @Column(name = "row_name", length = 500, nullable = true) // Specific row within a section
    private String row;

    @Column(name = "column_name", length = 500, nullable = true) // Specific column within a row, column is reserved name using col instead
    private String column;

    @Column(name = "shelf", length = 500, nullable = true) // Optional: Specific shelf on a rack
    private String shelf;

    @Column(name = "bin", length = 500, nullable = true) // Optional: Specific bin on a shelf for very small items
    private String bin;
    @Column(name = "level", length = 500, nullable = true) // Optional: Level within the warehouse or column or row or bin
    private String level;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getItemDescription() {
        return description;
    }

    public void setItemDescription(String itemDescription) {
        this.description = itemDescription;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getShelf() {
        return shelf;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    //toString method
    @Override
    public String toString() {
        return "Location{" +
                "id=" + id + '\'' +
                ", warehouse=" + warehouse + '\'' +
                ", locationName='" + locationName + '\'' +
                ", itemDescription='" + description + '\'' +
                ", section='" + section + '\'' +
                ", row='" + row + '\'' +
                ", column='" + column + '\'' +
                ", shelf='" + shelf + '\'' +
                ", bin='" + bin + '\'' +
                ", level='" + level + '\'' +
                ", baseEntity=" + super.toString() + '\'' +
                '}';
    }



}

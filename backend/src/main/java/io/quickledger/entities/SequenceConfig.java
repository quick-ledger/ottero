package io.quickledger.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "sequence_config", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "entity_type", "company_id" }),
        @UniqueConstraint(columnNames = { "entity_type", "company_id", "prefix", "postfix", "current_number" })
})
public class SequenceConfig {
    public enum EntityType {
        INVOICE,
        QUOTE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "prefix", length = 100)
    private String prefix;

    @Column(name = "postfix", length = 100)
    private String postfix;

    @Column(name = "current_number", nullable = false)
    private Integer currentNumber;

    @Column(name = "number_padding", nullable = false, columnDefinition = "integer default 4")
    private Integer numberPadding = 4; // Default to 4 digits (e.g., 0001)

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }

    public Integer getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(Integer currentNumber) {
        this.currentNumber = currentNumber;
    }

    public Integer getNumberPadding() {
        return numberPadding;
    }

    public void setNumberPadding(Integer numberPadding) {
        this.numberPadding = numberPadding;
    }

    @Override
    public String toString() {
        return "SequenceConfig{" +
                "id=" + id + '\'' +
                ", entityType=" + entityType + '\'' +
                ", companyId=" + companyId + '\'' +
                ", prefix='" + prefix + '\'' +
                ", postfix='" + postfix + '\'' +
                ", currentNumber=" + currentNumber + '\'' +
                '}';
    }
}

package io.quickledger.dto;

import io.quickledger.entities.SequenceConfig;

public class SequenceConfigDTO {
    private Long id;
    private String entityType;
    private Long companyId;
    private String prefix;
    private String postfix;
    private Integer currentNumber;
    private Integer numberPadding;

    public SequenceConfigDTO(SequenceConfig.EntityType entityType, Long companyId, String prefix, String postfix,
            Integer currentNumber, Integer numberPadding) {
        this.entityType = entityType.toString();
        this.companyId = companyId;
        this.prefix = prefix;
        this.postfix = postfix;
        this.currentNumber = currentNumber;
        this.numberPadding = numberPadding;
    }
    // Enum definition inside DTO

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
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

    @Override
    public String toString() {
        return "SequenceConfigDTO{" +
                "id=" + id +
                ", entityType='" + entityType + '\'' +
                ", companyId=" + companyId +
                ", prefix='" + prefix + '\'' +
                ", postfix='" + postfix + '\'' +
                ", currentNumber=" + currentNumber +
                '}';
    }

    /*
     * public Integer getStartNumber() {
     * return startNumber;
     * }
     * 
     * public void setStartNumber(Integer startNumber) {
     * this.startNumber = startNumber;
     * }
     */

    // public Integer getRevision() {
    // return revision;
    // }
    //
    // public void setRevision(Integer revision) {
    // this.revision = revision;
    // }

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
}

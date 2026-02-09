package io.quickledger.dto;

import java.time.LocalDateTime;

public class BaseEntityDto {
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String description;

    // getters and setters

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //toString method
    @Override
    public String toString() {
        return "BaseEntityDto{" +
                "createdDate=" + createdDate +
                ", modifiedDate=" + modifiedDate +
                ", description='" + description + '\'' +
                '}';
    }
}
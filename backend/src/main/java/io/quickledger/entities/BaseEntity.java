package io.quickledger.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@MappedSuperclass
public abstract class BaseEntity {
    @Column(name = "created_date", nullable = true)
    private LocalDateTime createdDate;

    @Column(name = "modified_date", nullable = true)
    private LocalDateTime modifiedDate;

    @Column(name = "description", length = 4000, nullable = true)
    private String description;// TODO do we need this? most entities will have their own description

    /*TODO: MBH: add createdUser and modifiedUser for userTracking and auditing in future.
       Need to find a way to inject the user from spring security here by default so that we don't have to always set it

          private String createdUser;
          private String modifiedUser;
     */
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

    public String getItemDescription() {
        return description;
    }

    public void setItemDescription(String itemDescription) {
        this.description = itemDescription;
    }

    //INFO: setting createdDate and modifiedDate before persisting and updating by default we no longer have to set it
    @PrePersist
    public void prePersist() {
        //MBH: updated it to use UTC time
        LocalDateTime utcNow = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        this.createdDate = utcNow;
        this.modifiedDate = utcNow;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedDate = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    }

    //toString method
    @Override
    public String toString() {
        return "BaseEntity{" +
                "createdDate=" + createdDate +
                ", modifiedDate=" + modifiedDate +
                ", description='" + description + '\'' +
                '}';
    }
}

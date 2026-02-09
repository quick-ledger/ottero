package io.quickledger.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/*
TODO: We may remove this table going forward as we already have it in KeyCloak database.
This one won't extend BaseEntity because it's not a part of the QuickLedger application.
 */
@Entity
@Table(name = "kc_events")
public class KcEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType;

    @Column(length = 1000)
    private String eventDetails;

    @Column(name="event_date")
    private LocalDateTime eventDateTime;

    @Column(name="client_id")
    private String clientID;

    @Column(name="ip_address")
    private String ipAddress;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    //toString method
    @Override
    public String toString() {
        return "KcEvent{" +
                "id=" + id + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventDetails='" + eventDetails + '\'' +
                ", eventDateTime=" + eventDateTime +
                ", clientID='" + clientID + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
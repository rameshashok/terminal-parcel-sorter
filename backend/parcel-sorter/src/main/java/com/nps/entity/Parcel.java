package com.nps.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parcels")
public class Parcel extends PanacheEntity {

    public String trackingNumber;
    public String origin;
    public String destination;
    public String postalCode;
    public double weightKg;

    @Enumerated(EnumType.STRING)
    public ParcelStatus status;

    public String assignedBelt;
    @Column(columnDefinition = "TEXT")
    public String aiReasoning;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}

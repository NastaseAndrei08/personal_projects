package com.example.demo.monitoring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "monitored_devices")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MonitoredDevice {
    @Id
    private UUID id;
    private Double maxConsumption;
    private String ownerUsername;
}
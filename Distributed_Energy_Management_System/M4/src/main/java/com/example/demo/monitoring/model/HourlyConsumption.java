package com.example.demo.monitoring.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hourly_consumption")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HourlyConsumption {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    @Column(nullable = false)
    private LocalDateTime timestamp; // Stores the hour (e.g., 2024-10-10 14:00)

    @Column(nullable = false)
    private Double totalConsumption;
}
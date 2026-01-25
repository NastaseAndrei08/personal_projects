package com.example.demo.device.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    // max hourly energy consumption, for example
    private Double maxConsumption;

    // username of the assigned user; can be null
    private String ownerUsername;
}

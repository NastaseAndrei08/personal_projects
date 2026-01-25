package com.example.demo.device.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "device_users") // Local copy of users
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeviceUser {
    @Id
    private UUID id;
    private String username;
}
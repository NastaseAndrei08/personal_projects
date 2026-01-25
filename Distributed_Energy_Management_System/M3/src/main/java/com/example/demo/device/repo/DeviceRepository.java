package com.example.demo.device.repo;

import com.example.demo.device.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findByOwnerUsername(String ownerUsername);
}

package com.example.demo.monitoring.repo;

import com.example.demo.monitoring.model.MonitoredDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MonitoredDeviceRepository extends JpaRepository<MonitoredDevice, UUID> {
}
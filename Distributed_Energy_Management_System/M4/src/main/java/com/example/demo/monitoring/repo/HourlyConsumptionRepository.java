package com.example.demo.monitoring.repo;

import com.example.demo.monitoring.model.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, UUID> {
    Optional<HourlyConsumption> findByDeviceIdAndTimestamp(UUID deviceId, LocalDateTime timestamp);
    List<HourlyConsumption> findByDeviceIdAndTimestampBetween(UUID deviceId, LocalDateTime start, LocalDateTime end);

}
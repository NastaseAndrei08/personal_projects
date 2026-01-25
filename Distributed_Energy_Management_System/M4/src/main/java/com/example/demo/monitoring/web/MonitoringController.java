package com.example.demo.monitoring.web;

import com.example.demo.monitoring.model.HourlyConsumption;
import com.example.demo.monitoring.repo.HourlyConsumptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final HourlyConsumptionRepository repo;

    @GetMapping("/history/{deviceId}")
    public List<HourlyConsumption> getHistory(
            @PathVariable UUID deviceId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return repo.findByDeviceIdAndTimestampBetween(deviceId, start, end);
    }
}
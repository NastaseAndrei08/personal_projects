package com.example.demo.monitoring.consumer;

import com.example.demo.monitoring.model.HourlyConsumption;
import com.example.demo.monitoring.model.MonitoredDevice;
import com.example.demo.monitoring.repo.HourlyConsumptionRepository;
import com.example.demo.monitoring.repo.MonitoredDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageConsumer {

    private final HourlyConsumptionRepository repo;
    private final MonitoredDeviceRepository deviceRepo;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "${monitoring.queue.name}")
    public void consumeSensorData(Map<String, Object> message) {
        try {
            Long timestamp = ((Number) message.get("timestamp")).longValue();
            String deviceIdStr = (String) message.get("device_id");
            Double measurementValue = ((Number) message.get("measurement_value")).doubleValue();

            UUID deviceId = UUID.fromString(deviceIdStr);
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            LocalDateTime hourKey = dateTime.withMinute(0).withSecond(0).withNano(0);

            HourlyConsumption record;
            Optional<HourlyConsumption> existing = repo.findByDeviceIdAndTimestamp(deviceId, hourKey);

            if (existing.isPresent()) {
                record = existing.get();
                record.setTotalConsumption(record.getTotalConsumption() + measurementValue);
            } else {
                record = HourlyConsumption.builder()
                        .deviceId(deviceId)
                        .timestamp(hourKey)
                        .totalConsumption(measurementValue)
                        .build();
            }
            repo.save(record);

            checkAndAlert(deviceId, record.getTotalConsumption(), timestamp);

        } catch (Exception e) {
            log.error("Failed to process message", e);
        }
    }

    private void checkAndAlert(UUID deviceId, double currentTotal, long timestamp) {
        Optional<MonitoredDevice> deviceOpt = deviceRepo.findById(deviceId);

        if (deviceOpt.isPresent()) {
            MonitoredDevice device = deviceOpt.get();
            double maxLimit = device.getMaxConsumption();

            if (currentTotal > maxLimit) {
                log.warn("ALERT: Device {} exceeded limit! (Current: {}, Max: {})", deviceId, currentTotal, maxLimit);

                Map<String, Object> alertMsg = new HashMap<>();
                alertMsg.put("device_id", deviceId.toString());
                alertMsg.put("message", "High Energy Consumption Alert! Limit: " + maxLimit + " kW");
                alertMsg.put("current_value", currentTotal);
                alertMsg.put("timestamp", timestamp);

                // [FIX] Use ownerUsername (String) instead of UUID
                if (device.getOwnerUsername() != null) {
                    alertMsg.put("user_id", device.getOwnerUsername());
                } else {
                    alertMsg.put("user_id", "unknown");
                }

                rabbitTemplate.convertAndSend("sync_exchange", "notification.alert", alertMsg);
            }
        }
    }

    @RabbitListener(queues = "m4_sync_inbox")
    public void consumeDeviceSync(Map<String, Object> message) {
        try {
            String event = (String) message.get("event");
            if ("DEVICE_CREATED_OR_UPDATED".equals(event)) {
                UUID id = UUID.fromString((String) message.get("device_id"));
                Double max = ((Number) message.get("max_consumption")).doubleValue();

                // [FIX] Read as String directly. Do NOT convert to UUID.
                String username = (String) message.get("user_id");

                MonitoredDevice device = new MonitoredDevice(id, max, username);
                deviceRepo.save(device);
                log.info("Synced device in M4: {} (User: {})", id, username);
            }
            else if ("DEVICE_DELETED".equals(event)) {
                UUID id = UUID.fromString((String) message.get("device_id"));
                deviceRepo.deleteById(id);
            }
        } catch (Exception e) {
            log.error("Failed to sync device", e);
        }
    }
}
package com.example.demo.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;

@Service
public class LoadBalancerService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.replica.count:2}")
    private int replicaCount;

    public LoadBalancerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${app.input.queue}")
    public void distributeMessage(Map<String, Object> message) {
        try {
            String deviceIdStr = (String) message.get("device_id");
            if (deviceIdStr != null) {
                // Consistent Hashing: Device X always goes to Replica Y
                UUID deviceId = UUID.fromString(deviceIdStr);
                int hash = Math.abs(deviceId.hashCode());
                int replicaIndex = hash % replicaCount;

                String targetQueue = "sensor_queue_" + replicaIndex;
                rabbitTemplate.convertAndSend(targetQueue, message);

                System.out.println("Routed " + deviceIdStr + " -> " + targetQueue);
            }
        } catch (Exception e) {
            System.err.println("LB Error: " + e.getMessage());
        }
    }
}
package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // Listen to "notification_queue"
    // The 'message' parameter is automatically converted from JSON to Map
    @RabbitListener(queues = "notification_queue")
    public void handleNotification(Map<String, Object> message) {
        try {
            log.info(">>> M5 Received Alert from RabbitMQ: {}", message);

            // 1. Extract the User ID
            String userId = (String) message.get("user_id");

            // 2. Push to WebSocket
            // Only send if we know who the user is
            if (userId != null && !userId.isEmpty() && !userId.equals("unknown")) {

                // Construct the destination: /topic/alerts/{userId}
                String destination = "/topic/alerts/" + userId;

                // Push the entire message (contains timestamp, device_id, message, etc.)
                messagingTemplate.convertAndSend(destination, message);

                log.info(">>> Forwarded to WebSocket Topic: {}", destination);
            } else {
                log.warn("Alert received without a valid User ID. Cannot route to specific client.");
            }
        } catch (Exception e) {
            log.error("Error processing notification", e);
        }
    }
}
package com.example.demo.device.consumer;

import com.example.demo.device.model.DeviceUser;
import com.example.demo.device.repo.DeviceUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncConsumer {

    private final DeviceUserRepository userRepo;

    @RabbitListener(queues = "m3_sync_inbox")
    public void consumeSync(Map<String, Object> message) {
        try {
            String event = (String) message.get("event");

            // 1. USER CREATED
            if ("USER_CREATED".equals(event)) {
                UUID id = UUID.fromString((String) message.get("user_id"));
                String username = (String) message.get("username");
                DeviceUser user = new DeviceUser(id, username);
                userRepo.save(user);
                log.info("Synced user created in M3: {}", username);
            }
            // 2. USER DELETED (New)
            else if ("USER_DELETED".equals(event)) {
                UUID id = UUID.fromString((String) message.get("user_id"));
                if (userRepo.existsById(id)) {
                    userRepo.deleteById(id);
                    log.info("Synced user deleted in M3: {}", id);

                    // Optional: You could also unassign devices belonging to this user here
                    // deviceRepo.unassignAllFromUser(id);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process sync message in M3: {}", message, e);
        }
    }
}
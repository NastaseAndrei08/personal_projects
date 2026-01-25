package com.example.demo.device.web;

import com.example.demo.device.model.Device;
import com.example.demo.device.repo.DeviceRepository;
import com.example.demo.device.web.dto.DeviceDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.example.demo.device.repo.DeviceUserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRepository repo;
    private final RabbitTemplate rabbitTemplate;
    private final DeviceUserRepository userRepo;

    private void requireAdmin(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (role == null || !"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN role required");
        }
    }

    private DeviceDto toDto(Device d) {
        return new DeviceDto(
                d.getId(),
                d.getName(),
                d.getMaxConsumption(),
                d.getOwnerUsername()
        );
    }

    private void apply(Device d, DeviceDto dto) {
        if (dto.name() != null) d.setName(dto.name());
        if (dto.maxConsumption() != null) d.setMaxConsumption(dto.maxConsumption());
        if (dto.ownerUsername() != null) d.setOwnerUsername(dto.ownerUsername());
    }


    private void sendSync(String event, Device d) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("event", event);
        msg.put("device_id", d.getId().toString());
        msg.put("max_consumption", d.getMaxConsumption());
        msg.put("user_id", d.getOwnerUsername()); // Note: In A2 usually we use User ID (UUID), but Username works if consistent.

        rabbitTemplate.convertAndSend("sync_exchange", "device.event", msg);
    }

    // ---------- ADMIN CRUD on /api/devices ----------

    @GetMapping("/api/devices")
    public List<DeviceDto> getAll(HttpServletRequest request) {
        requireAdmin(request);
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @GetMapping("/api/devices/{id}")
    public DeviceDto getOne(@PathVariable UUID id, HttpServletRequest request) {
        requireAdmin(request);
        var d = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
        return toDto(d);
    }

    @PostMapping("/api/devices")
    public DeviceDto create(@RequestBody DeviceDto dto, HttpServletRequest request) {
        requireAdmin(request);
        if (dto.name() == null || dto.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        var entity = Device.builder()
                .id(dto.id() != null ? dto.id() : UUID.randomUUID())
                .name(dto.name())
                .maxConsumption(dto.maxConsumption())
                .ownerUsername(dto.ownerUsername())
                .build();
        entity = repo.save(entity);

        sendSync("DEVICE_CREATED_OR_UPDATED", entity);
        return toDto(entity);
    }

    @PutMapping("/api/devices/{id}")
    public DeviceDto update(@PathVariable UUID id,
                            @RequestBody DeviceDto dto,
                            HttpServletRequest request) {
        requireAdmin(request);
        var entity = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        apply(entity, dto);
        entity = repo.save(entity);

        sendSync("DEVICE_CREATED_OR_UPDATED", entity);
        return toDto(entity);
    }

    @DeleteMapping("/api/devices/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, HttpServletRequest request) {
        requireAdmin(request);
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found");
        }
        repo.deleteById(id);

        Map<String, Object> msg = new HashMap<>();
        msg.put("event", "DEVICE_DELETED");
        msg.put("device_id", id.toString());
        rabbitTemplate.convertAndSend("sync_exchange", "device.deleted", msg);
    }



    // ---------- ADMIN assignment endpoints ----------

    @PutMapping("/api/devices/{id}/assign")
    public DeviceDto assign(@PathVariable UUID id,
                            @RequestParam String username,
                            HttpServletRequest request) {
        requireAdmin(request);

        // 2. VALIDATION: Check LOCAL database (Decoupled!)
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User '" + username + "' does not exist (not synced yet?)"));

        var entity = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        entity.setOwnerUsername(username); // You could also store user.getId() now if you prefer UUIDs
        entity = repo.save(entity);

        sendSync("DEVICE_CREATED_OR_UPDATED", entity);
        return toDto(entity);
    }

    @PutMapping("/api/devices/{id}/unassign")
    public DeviceDto unassign(@PathVariable UUID id, HttpServletRequest request) {
        requireAdmin(request);
        var entity = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
        entity.setOwnerUsername(null);
        entity = repo.save(entity);
        return toDto(entity);
    }

    // ---------- "My devices" (ADMIN or CLIENT) ----------

    @GetMapping("/api/my/devices")
    public List<DeviceDto> myDevices(HttpServletRequest request) {
        String username = request.getHeader("X-User-Name");
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Name");
        }
        return repo.findByOwnerUsername(username).stream().map(this::toDto).toList();
    }
}

package com.example.demo.user.web;

import com.example.demo.user.model.UserProfile;
import com.example.demo.user.repo.UserProfileRepository;
import com.example.demo.user.web.dto.UserProfileDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileRepository repo;
    private final RabbitTemplate rabbitTemplate;

    /** Gateway should already enforce roles, but we double-check. */
    private void requireAdmin(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (role == null || !"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN role required");
        }
    }

    private UserProfileDto toDto(UserProfile u) {
        return new UserProfileDto(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getRole());
    }

    @GetMapping
    public List<UserProfileDto> getAll(HttpServletRequest request) {
        requireAdmin(request);
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public UserProfileDto getOne(@PathVariable UUID id, HttpServletRequest request) {
        requireAdmin(request);
        var u = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toDto(u);
    }

    @PostMapping
    public UserProfileDto create(@RequestBody UserProfileDto dto, HttpServletRequest request) {
        requireAdmin(request);
        if (dto.username() == null || dto.username().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is required");
        }
        var entity = UserProfile.builder()
                .id(dto.id() != null ? dto.id() : UUID.randomUUID())
                .username(dto.username())
                .fullName(dto.fullName())
                .email(dto.email())
                .role(dto.role())
                .build();
        entity = repo.save(entity);

        // 2. SEND SYNC MESSAGE (The Requirement)
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "USER_CREATED");
            msg.put("user_id", entity.getId().toString());
            msg.put("username", entity.getUsername());

            rabbitTemplate.convertAndSend("sync_exchange", "user.created", msg);

        } catch (Exception e) {
            System.err.println("Failed to send user sync: " + e.getMessage());
        }

        return toDto(entity);
    }

    @PutMapping("/{id}")
    public UserProfileDto update(@PathVariable UUID id,
                                 @RequestBody UserProfileDto dto,
                                 HttpServletRequest request) {
        requireAdmin(request);
        var entity = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (dto.username() != null) entity.setUsername(dto.username());
        if (dto.fullName() != null) entity.setFullName(dto.fullName());
        if (dto.email() != null) entity.setEmail(dto.email());
        if (dto.role() != null) entity.setRole(dto.role());

        entity = repo.save(entity);
        return toDto(entity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, HttpServletRequest request) {
        requireAdmin(request);
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        repo.deleteById(id);

        // --- NEW: SEND SYNC MESSAGE (USER_DELETED) ---
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "USER_DELETED");
            msg.put("user_id", id.toString());
            rabbitTemplate.convertAndSend("sync_exchange", "user.deleted", msg);
        } catch (Exception e) {
            System.err.println("Failed to send delete sync: " + e.getMessage());
        }
        // ---------------------------------------------
    }

    // Used by M3 to verify if a user exists before assigning a device
    @GetMapping("/by-username/{username}")
    public UserProfileDto getByUsername(@PathVariable String username, HttpServletRequest request) {
        // We enforce Admin security so only M3 (spoofing Admin) or real Admins can call this
        requireAdmin(request);

        var u = repo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + username + " not found"));
        return toDto(u);
    }
}

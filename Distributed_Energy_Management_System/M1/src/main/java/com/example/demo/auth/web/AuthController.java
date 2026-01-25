package com.example.demo.auth.web;

import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.repo.AuthUserRepository;
import com.example.demo.auth.security.JwtUtil;
import com.example.demo.auth.web.dto.LoginRequest;
import com.example.demo.auth.web.dto.RegisterRequest;
import com.example.demo.auth.web.dto.TokenResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthUserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    private final RestTemplate restTemplate; // Injected from AppConfig

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "OK"); }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Validated RegisterRequest r) {
        // 1. Check if user exists locally
        if (repo.existsByUsername(r.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
        }

        UUID newId = UUID.randomUUID();

        // 2. Save Auth Credentials (M1)
        var user = AuthUser.builder()
                .id(newId)
                .username(r.username())
                .passwordHash(encoder.encode(r.password()))
                .role(r.role())
                .build();
        repo.save(user);

        // 3. Sync with User Service (M2) using Request-Reply
        // We define the M2 URL (service name 'm2' comes from docker-compose)
        String m2Url = "http://m2:8080/api/users";

        try {
            // Prepare the payload for M2.
            // Note: We use a Map here to avoid creating a UserProfileDto class in M1
            Map<String, Object> userProfile = Map.of(
                    "id", newId,
                    "username", r.username(),
                    "fullName", r.username(), // Defaulting full name to username
                    "email", r.username() + "@example.com", // Default email
                    "role", r.role()
            );

            // Prepare Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // We must spoof the ADMIN role because M2's controller requires it
            headers.set("X-User-Role", "ADMIN");

            // Execute the request
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(userProfile, headers);
            restTemplate.postForEntity(m2Url, request, Void.class);

        } catch (Exception e) {
            // If M2 is down or fails, we log it.
            // In a strict distributed transaction, we might roll back, but for this assignment, logging is acceptable.
            System.err.println("SYNC ERROR: Failed to create user profile in M2: " + e.getMessage());
            // Optional: repo.delete(user); // Uncomment if you want to fail the registration if M2 fails
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to sync user profile");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Validated LoginRequest r) {
        var user = repo.findByUsername(r.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "bad credentials"));
        if (!encoder.matches(r.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "bad credentials");
        }

        return new TokenResponse(jwt.generate(user.getUsername(), user.getRole()));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token");
        }

        String token = authHeader.substring(7);

        try {
            Claims c = jwt.parse(token);
            String username = c.getSubject();
            String role = c.get("role", String.class);

            // Response body for debugging
            Map<String, Object> body = Map.of(
                    "valid", true,
                    "username", username,
                    "role", role,
                    "exp", c.getExpiration().getTime()
            );

            // Headers for Traefik to forward to downstream services (M2/M3)
            return ResponseEntity.ok()
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .body(body);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }
}
package com.example.demo.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "auth_users",
        uniqueConstraints = @UniqueConstraint(name = "uk_auth_username", columnNames = "username"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthUser {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role; // ADMIN or CLIENT
}

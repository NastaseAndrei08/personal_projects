package com.example.demo.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_username", columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    private String fullName;

    private String email;

    // For front-end display; real authority from JWT/Auth
    private String role; // ADMIN or CLIENT
}

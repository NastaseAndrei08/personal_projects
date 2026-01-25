package com.example.demo.user.repo;

import com.example.demo.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUsername(String username);
}

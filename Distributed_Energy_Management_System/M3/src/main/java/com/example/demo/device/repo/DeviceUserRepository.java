package com.example.demo.device.repo;

import com.example.demo.device.model.DeviceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DeviceUserRepository extends JpaRepository<DeviceUser, UUID> {
    Optional<DeviceUser> findByUsername(String username);
}
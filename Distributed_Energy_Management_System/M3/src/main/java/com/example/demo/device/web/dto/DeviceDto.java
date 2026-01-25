package com.example.demo.device.web.dto;

import java.util.UUID;

public record DeviceDto(
        UUID id,
        String name,
        Double maxConsumption,
        String ownerUsername
) {}

package de.technikteam.model.dto;

import java.time.LocalDateTime;

public record LoginIpInfo(
        String ipAddress,
        String countryCode,
        String deviceType,
        LocalDateTime lastSeen
) {}
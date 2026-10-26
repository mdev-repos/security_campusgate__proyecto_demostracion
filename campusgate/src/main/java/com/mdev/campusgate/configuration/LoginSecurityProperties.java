package com.mdev.campusgate.configuration;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.security.login")
@Validated
public record LoginSecurityProperties(
        @Positive int maxFailedAttempts,
        @Positive long lockMinutes
) {}
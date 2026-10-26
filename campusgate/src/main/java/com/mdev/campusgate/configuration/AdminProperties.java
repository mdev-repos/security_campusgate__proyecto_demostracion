package com.mdev.campusgate.configuration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.admin")
@Validated
public record AdminProperties(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 12) String password
) {}
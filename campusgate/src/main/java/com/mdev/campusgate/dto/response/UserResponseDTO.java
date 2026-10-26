package com.mdev.campusgate.dto.response;

import com.mdev.campusgate.model.enums.Role;

import java.util.Set;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String email,
        Set<Role> roles,
        boolean enabled,
        boolean notLocked
) {}
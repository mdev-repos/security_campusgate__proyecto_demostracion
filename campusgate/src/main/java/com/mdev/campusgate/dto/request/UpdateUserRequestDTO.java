package com.mdev.campusgate.dto.request;

import com.mdev.campusgate.model.enums.Role;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateUserRequestDTO(
        @Size(min = 1) Set<Role> roles,
        Boolean enabled,
        Boolean accountNotLocked
) {}
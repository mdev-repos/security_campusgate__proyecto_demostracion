package com.mdev.campusgate.dto.request;

import com.mdev.campusgate.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequestDTO(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotEmpty Set<Role> roles
) {}
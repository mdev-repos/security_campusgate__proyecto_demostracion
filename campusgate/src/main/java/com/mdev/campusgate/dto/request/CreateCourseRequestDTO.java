package com.mdev.campusgate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCourseRequestDTO(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 1000) String description,
        @NotNull UUID professorId
) {}
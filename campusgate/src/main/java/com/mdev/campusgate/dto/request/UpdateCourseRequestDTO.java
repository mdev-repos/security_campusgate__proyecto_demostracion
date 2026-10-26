package com.mdev.campusgate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCourseRequestDTO(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 1000) String description
) {}
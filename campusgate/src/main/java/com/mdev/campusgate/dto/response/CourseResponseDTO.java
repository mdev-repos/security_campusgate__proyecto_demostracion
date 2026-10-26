package com.mdev.campusgate.dto.response;

import java.util.UUID;

public record CourseResponseDTO(
        UUID id,
        String title,
        String description,
        UUID professorId,
        int studentCount
) {}
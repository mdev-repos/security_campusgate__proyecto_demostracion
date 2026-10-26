package com.mdev.campusgate.dto.response;

import java.util.UUID;

public record ProfileResponseDTO(
        UUID id,
        UUID userId,
        String firstName,
        String lastName
) {}
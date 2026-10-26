package com.mdev.campusgate.dto.response;

public record LoginResponseDTO(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
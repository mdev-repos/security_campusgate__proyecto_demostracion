package com.mdev.campusgate.dto.response;

import java.util.List;

public record MeResponseDTO(
        String userId,
        List<String> permissions
) {}
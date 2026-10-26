package com.mdev.campusgate.mapper;

import com.mdev.campusgate.dto.response.ProfileResponseDTO;
import com.mdev.campusgate.model.Professor;
import com.mdev.campusgate.model.Student;

public class ProfileMapper {
    public static ProfileResponseDTO toResponse(Professor p) {
        return new ProfileResponseDTO(p.getId(), p.getUser().getId(), p.getFirstName(), p.getLastName());
    }

    public static ProfileResponseDTO toResponse(Student s) {
        return new ProfileResponseDTO(s.getId(), s.getUser().getId(), s.getFirstName(), s.getLastName());
    }
}
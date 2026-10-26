package com.mdev.campusgate.mapper;

import com.mdev.campusgate.dto.response.CourseResponseDTO;
import com.mdev.campusgate.model.Course;

public class CourseMapper {
    public static CourseResponseDTO toResponse(Course c) {
        return new CourseResponseDTO(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getProfessor().getId(),
                c.getStudents().size()
        );
    }
}
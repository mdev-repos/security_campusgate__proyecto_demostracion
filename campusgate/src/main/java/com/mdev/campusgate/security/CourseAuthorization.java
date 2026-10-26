package com.mdev.campusgate.security;

import com.mdev.campusgate.repository.ICourseRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("courseAuth")
public class CourseAuthorization {
    private final ICourseRepository courseRepo;

    public CourseAuthorization(ICourseRepository courseRepository) {
        this.courseRepo = courseRepository;
    }

    public boolean isOwner(UUID courseId, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return courseRepo.existsByIdAndProfessorUserId(courseId, userId);
    }
}
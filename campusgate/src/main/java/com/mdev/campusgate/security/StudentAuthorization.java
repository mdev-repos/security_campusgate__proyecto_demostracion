package com.mdev.campusgate.security;

import com.mdev.campusgate.repository.IStudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("studentAuth")
public class StudentAuthorization {
    private final IStudentRepository studentRepo;

    public StudentAuthorization(IStudentRepository studentRepository) {
        this.studentRepo = studentRepository;
    }

    public boolean isSelf(UUID studentId, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return studentRepo.existsByIdAndUserId(studentId, userId);
    }
}
package com.mdev.campusgate.service.impl;

import com.mdev.campusgate.dto.request.CreateProfileRequestDTO;
import com.mdev.campusgate.dto.response.ProfileResponseDTO;
import com.mdev.campusgate.exception.BusinessRuleException;
import com.mdev.campusgate.exception.ResourceNotFoundException;
import com.mdev.campusgate.mapper.ProfileMapper;
import com.mdev.campusgate.model.Student;
import com.mdev.campusgate.model.UserSec;
import com.mdev.campusgate.model.enums.Role;
import com.mdev.campusgate.repository.IStudentRepository;
import com.mdev.campusgate.repository.IUserRepository;
import com.mdev.campusgate.service.IStudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mdev.campusgate.dto.response.PageResponseDTO;
import com.mdev.campusgate.utils.PageableValidator;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class StudentService implements IStudentService {
    private static final Set<String> ALLOWED_SORT = Set.of("firstName", "lastName", "createdAt");

    private final IStudentRepository studentRepo;
    private final IUserRepository userRepo;

    public StudentService(IStudentRepository studentRepository, IUserRepository userRepository) {
        this.studentRepo = studentRepository;
        this.userRepo = userRepository;
    }

    @Override
    @Transactional
    public ProfileResponseDTO createStudent(CreateProfileRequestDTO dto) {
        UserSec user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        if (!user.getRoles().contains(Role.STUDENT)) {
            throw new BusinessRuleException("User does not have the STUDENT role");
        }
        if (studentRepo.existsByUserId(user.getId())) {
            throw new BusinessRuleException("Student profile already exists for this user");
        }

        Student student = new Student();
        student.setUser(user);
        student.setFirstName(dto.firstName());
        student.setLastName(dto.lastName());

        Student saved = studentRepo.save(student);
        log.info("Student profile created with id {}", saved.getId());
        return ProfileMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getStudentById(UUID id) {
        return studentRepo.findById(id)
                .map(ProfileMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Student"));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getStudentByUserId(UUID userId) {
        return studentRepo.findByUserId(userId)
                .map(ProfileMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Student"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProfileResponseDTO> getAllStudents(Pageable pageable) {
        PageableValidator.validateSort(pageable, ALLOWED_SORT);
        return PageResponseDTO.from(studentRepo.findAll(pageable).map(ProfileMapper::toResponse));
    }
}

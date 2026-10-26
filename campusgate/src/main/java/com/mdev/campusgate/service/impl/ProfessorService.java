package com.mdev.campusgate.service.impl;

import com.mdev.campusgate.dto.request.CreateProfileRequestDTO;
import com.mdev.campusgate.dto.response.ProfileResponseDTO;
import com.mdev.campusgate.exception.BusinessRuleException;
import com.mdev.campusgate.exception.ResourceNotFoundException;
import com.mdev.campusgate.mapper.ProfileMapper;
import com.mdev.campusgate.model.Professor;
import com.mdev.campusgate.model.UserSec;
import com.mdev.campusgate.model.enums.Role;
import com.mdev.campusgate.repository.IProfessorRepository;
import com.mdev.campusgate.repository.IUserRepository;
import com.mdev.campusgate.service.IProfessorService;
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
public class ProfessorService implements IProfessorService {
    private static final Set<String> ALLOWED_SORT = Set.of("firstName", "lastName", "createdAt");

    private final IProfessorRepository professorRepo;
    private final IUserRepository userRepo;

    public ProfessorService(IProfessorRepository professorRepository, IUserRepository userRepository) {
        this.professorRepo = professorRepository;
        this.userRepo = userRepository;
    }

    @Override
    @Transactional
    public ProfileResponseDTO createProfessor(CreateProfileRequestDTO dto) {
        UserSec user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        if (!user.getRoles().contains(Role.PROFESSOR)) {
            throw new BusinessRuleException("User does not have the PROFESSOR role");
        }
        if (professorRepo.existsByUserId(user.getId())) {
            throw new BusinessRuleException("Professor profile already exists for this user");
        }

        Professor professor = new Professor();
        professor.setUser(user);
        professor.setFirstName(dto.firstName());
        professor.setLastName(dto.lastName());

        Professor saved = professorRepo.save(professor);
        log.info("Professor profile created with id {}", saved.getId());
        return ProfileMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getProfessorById(UUID id) {
        return professorRepo.findById(id)
                .map(ProfileMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Professor"));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getProfessorByUserId(UUID userId) {
        return professorRepo.findByUserId(userId)
                .map(ProfileMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Professor"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProfileResponseDTO> getAllProfessors(Pageable pageable) {
        PageableValidator.validateSort(pageable, ALLOWED_SORT);
        return PageResponseDTO.from(professorRepo.findAll(pageable).map(ProfileMapper::toResponse));
    }
}

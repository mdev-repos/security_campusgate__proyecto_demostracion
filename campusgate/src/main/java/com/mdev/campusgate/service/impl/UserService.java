package com.mdev.campusgate.service.impl;

import com.mdev.campusgate.dto.request.CreateUserRequestDTO;
import com.mdev.campusgate.dto.request.UpdatePasswordRequestDTO;
import com.mdev.campusgate.dto.request.UpdateUserRequestDTO;
import com.mdev.campusgate.dto.response.UserResponseDTO;
import com.mdev.campusgate.dto.response.PageResponseDTO;
import com.mdev.campusgate.exception.BusinessRuleException;
import com.mdev.campusgate.exception.EmailAlreadyExistsException;
import com.mdev.campusgate.exception.InvalidRequestException;
import com.mdev.campusgate.exception.ResourceNotFoundException;
import com.mdev.campusgate.model.enums.Role;
import com.mdev.campusgate.repository.IProfessorRepository;
import com.mdev.campusgate.repository.IStudentRepository;
import com.mdev.campusgate.security.DemoAccountGuard;
import com.mdev.campusgate.utils.PageableValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.mdev.campusgate.service.ILoginAttemptService;
import org.springframework.transaction.annotation.Transactional;
import com.mdev.campusgate.mapper.UserMapper;
import com.mdev.campusgate.model.UserSec;
import com.mdev.campusgate.repository.IUserRepository;
import com.mdev.campusgate.service.IUserService;
import com.mdev.campusgate.utils.EmailNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements IUserService {
    private static final Set<String> ALLOWED_SORT = Set.of("email", "createdAt", "enabled");

    private final IUserRepository userRepo;
    private final IProfessorRepository professorRepo;
    private final IStudentRepository studentRepo;
    private final PasswordEncoder passwordEncoder;
    private final ILoginAttemptService loginAttemptServ;
    private final DemoAccountGuard demoGuard;

    public UserService(IUserRepository userRepository,
                       IProfessorRepository professorRepository,
                       IStudentRepository studentRepository,
                       PasswordEncoder passwordEncoder,
                       ILoginAttemptService loginAttemptService,
                       DemoAccountGuard demoGuard){
        this.userRepo = userRepository;
        this.professorRepo = professorRepository;
        this.studentRepo = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptServ = loginAttemptService;
        this.demoGuard = demoGuard;
    }

    @Override
    public UserResponseDTO createUser(CreateUserRequestDTO dto) {
        String normalizedEmail = EmailNormalizer.normalize(dto.email());

        boolean exists = userRepo.existsByEmail(normalizedEmail);
        if(exists) {
            throw new EmailAlreadyExistsException();
        }

        UserSec user = new UserSec();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setEnabled(true);
        user.setAccountNotLocked(true);
        user.setRoles(dto.roles());

        try {
            UserSec saved = userRepo.saveAndFlush(user);
            log.info("User created with id {}", saved.getId());
            return UserMapper.toUserResponseDTO(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyExistsException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID id) {
        return UserMapper.toUserResponseDTO(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<UserResponseDTO> getAllUsers(Pageable pageable) {
        PageableValidator.validateSort(pageable, ALLOWED_SORT);
        Page<UserResponseDTO> page = userRepo.findAll(pageable).map(UserMapper::toUserResponseDTO);
        return PageResponseDTO.from(page);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UUID id, UpdateUserRequestDTO dto, UUID actorId) {
        rejectSelfModification(id, actorId);

        if (dto.roles() == null && dto.enabled() == null && dto.accountNotLocked() == null) {
            throw new InvalidRequestException("No fields to update");
        }

        UserSec user = findOrThrow(id);
        demoGuard.assertNotProtected(user);

        if (dto.roles() != null) {
            if (!dto.roles().contains(Role.PROFESSOR) && professorRepo.existsByUserId(id)) {
                throw new BusinessRuleException("User has a professor profile; PROFESSOR role cannot be removed");
            }
            if (!dto.roles().contains(Role.STUDENT) && studentRepo.existsByUserId(id)) {
                throw new BusinessRuleException("User has a student profile; STUDENT role cannot be removed");
            }
            user.getRoles().clear();
            user.getRoles().addAll(dto.roles());
        }
        if (dto.enabled() != null) {
            user.setEnabled(dto.enabled());
        }
        if (dto.accountNotLocked() != null) {
            user.setAccountNotLocked(dto.accountNotLocked());
            if (dto.accountNotLocked()) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
            }
        }

        log.info("User {} updated by {}", id, actorId);
        return UserMapper.toUserResponseDTO(user);
    }

    @Override
    @Transactional
    public void disableUser(UUID id, UUID actorId) {
        rejectSelfModification(id, actorId);
        UserSec user = findOrThrow(id);
        demoGuard.assertNotProtected(user);
        user.setEnabled(false);
        log.info("User {} disabled by {}", id, actorId);
    }

    @Override
    @Transactional
    public void updatePassword(UUID userId, UpdatePasswordRequestDTO dto) {
        UserSec user = findOrThrow(userId);
        demoGuard.assertNotProtected(user);

        if (user.isTemporarilyLocked()) {
            throw new BusinessRuleException("Account temporarily locked");
        }

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPasswordHash())) {
            loginAttemptServ.recordFailure(user.getEmail());
            throw new InvalidRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(dto.newPassword(), user.getPasswordHash())) {
            throw new InvalidRequestException("New password must be different from the current one");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        loginAttemptServ.recordSuccess(user.getEmail());
        log.info("Password changed for user {}", userId);
    }

    private UserSec findOrThrow(UUID id) {
        return userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User"));
    }

    private void rejectSelfModification(UUID id, UUID actorId) {
        if (id.equals(actorId)) {
            throw new BusinessRuleException("You cannot modify your own account");
        }
    }
}
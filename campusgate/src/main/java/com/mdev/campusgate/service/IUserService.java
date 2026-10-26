package com.mdev.campusgate.service;

import com.mdev.campusgate.dto.request.CreateUserRequestDTO;
import com.mdev.campusgate.dto.request.UpdatePasswordRequestDTO;
import com.mdev.campusgate.dto.request.UpdateUserRequestDTO;
import com.mdev.campusgate.dto.response.PageResponseDTO;
import com.mdev.campusgate.dto.response.UserResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IUserService {
    UserResponseDTO createUser(CreateUserRequestDTO dto);
    UserResponseDTO getUserById(UUID id);
    PageResponseDTO<UserResponseDTO> getAllUsers(Pageable pageable);
    UserResponseDTO updateUser(UUID id, UpdateUserRequestDTO dto, UUID actorId);
    void disableUser(UUID id, UUID actorId);
    void updatePassword(UUID userId, UpdatePasswordRequestDTO dto);
}
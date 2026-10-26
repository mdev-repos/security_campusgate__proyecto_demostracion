package com.mdev.campusgate.service;

import com.mdev.campusgate.dto.request.CreateProfileRequestDTO;
import com.mdev.campusgate.dto.response.ProfileResponseDTO;

import com.mdev.campusgate.dto.response.PageResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IProfessorService {
    ProfileResponseDTO createProfessor(CreateProfileRequestDTO dto);
    ProfileResponseDTO getProfessorById(UUID id);
    ProfileResponseDTO getProfessorByUserId(UUID userId);
    PageResponseDTO<ProfileResponseDTO> getAllProfessors(Pageable pageable);
}
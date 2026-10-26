package com.mdev.campusgate.service;

import com.mdev.campusgate.dto.request.CreateProfileRequestDTO;
import com.mdev.campusgate.dto.response.ProfileResponseDTO;

import com.mdev.campusgate.dto.response.PageResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IStudentService {
    ProfileResponseDTO createStudent(CreateProfileRequestDTO dto);
    ProfileResponseDTO getStudentById(UUID id);
    ProfileResponseDTO getStudentByUserId(UUID userId);
    PageResponseDTO<ProfileResponseDTO> getAllStudents(Pageable pageable);
}
package com.mdev.campusgate.service;

import com.mdev.campusgate.dto.request.CreateCourseRequestDTO;
import com.mdev.campusgate.dto.request.UpdateCourseRequestDTO;
import com.mdev.campusgate.dto.response.CourseResponseDTO;
import com.mdev.campusgate.dto.response.PageResponseDTO;
import com.mdev.campusgate.dto.response.ProfileResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ICourseService {
    CourseResponseDTO createCourse(CreateCourseRequestDTO dto);
    CourseResponseDTO getCourseById(UUID id);
    PageResponseDTO<CourseResponseDTO> getAllCourses(Pageable pageable);
    CourseResponseDTO updateCourse(UUID id, UpdateCourseRequestDTO dto);
    void enrollStudent(UUID courseId, UUID studentId);
    void unenrollStudent(UUID courseId, UUID studentId);
    void deleteCourse(UUID id);
    PageResponseDTO<CourseResponseDTO> getCoursesByProfessorUser(UUID userId, Pageable pageable);
    PageResponseDTO<CourseResponseDTO> getCoursesByStudentUser(UUID userId, Pageable pageable);
    PageResponseDTO<ProfileResponseDTO> getCourseStudents(UUID courseId, Pageable pageable);
}
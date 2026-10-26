package com.mdev.campusgate.repository;

import com.mdev.campusgate.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ICourseRepository extends JpaRepository<Course, UUID> {
    boolean existsByIdAndProfessorUserId(UUID courseId, UUID userId);
    Page<Course> findByProfessorUserId(UUID userId, Pageable pageable);
    Page<Course> findByStudentsUserId(UUID userId, Pageable pageable);
}
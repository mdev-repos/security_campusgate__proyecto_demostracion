package com.mdev.campusgate.repository;

import com.mdev.campusgate.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IStudentRepository extends JpaRepository<Student, UUID> {
    boolean existsByUserId(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
    Optional<Student> findByUserId(UUID userId);

    @Query("select s from Student s where exists " +
            "(select 1 from Course c join c.students cs where c.id = :courseId and cs = s)")
    Page<Student> findByCourseId(@Param("courseId") UUID courseId, Pageable pageable);
}
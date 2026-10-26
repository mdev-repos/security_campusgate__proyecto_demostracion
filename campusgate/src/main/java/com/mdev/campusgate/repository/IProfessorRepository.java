package com.mdev.campusgate.repository;

import com.mdev.campusgate.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IProfessorRepository extends JpaRepository<Professor, UUID> {
    boolean existsByUserId(UUID userId);
    Optional<Professor> findByUserId(UUID userId);
}
package com.mdev.campusgate.repository;

import com.mdev.campusgate.model.UserSec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserRepository extends JpaRepository<UserSec, UUID> {
    Optional<UserSec> findByEmail(String email);
    boolean existsByEmail(String email);
}
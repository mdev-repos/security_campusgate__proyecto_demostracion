package com.mdev.campusgate.configuration;

import com.mdev.campusgate.model.UserSec;
import com.mdev.campusgate.model.enums.Role;
import com.mdev.campusgate.repository.IUserRepository;
import com.mdev.campusgate.utils.EmailNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class AdminSeeder implements ApplicationRunner {
    private final IUserRepository userRepo;
    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(IUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AdminProperties adminProperties){
        this.userRepo = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminProperties = adminProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String normalizedEmail = EmailNormalizer.normalize(adminProperties.email());
        boolean exists = userRepo.existsByEmail(normalizedEmail);
        if(exists){
            log.info("Admin user already exists, skipping seed");
        }
        else {
            Set<Role> role = new HashSet<>();
            role.add(Role.ADMIN);
            UserSec admin = new UserSec();
            admin.setEmail(normalizedEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminProperties.password()));
            admin.setEnabled(true);
            admin.setAccountNotLocked(true);
            admin.setRoles(role);

            UserSec saved = userRepo.save(admin);
            log.info("Admin user created with id {}", saved.getId());
        }
    }
}
package com.mdev.campusgate.service.impl;

import com.mdev.campusgate.configuration.LoginSecurityProperties;
import com.mdev.campusgate.model.UserSec;
import com.mdev.campusgate.repository.IUserRepository;
import com.mdev.campusgate.security.DemoAccountGuard;
import com.mdev.campusgate.service.ILoginAttemptService;
import com.mdev.campusgate.utils.EmailNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class LoginAttemptService implements ILoginAttemptService {
    private final IUserRepository userRepo;
    private final LoginSecurityProperties props;
    private final DemoAccountGuard demoGuard;

    public LoginAttemptService(IUserRepository userRepository, LoginSecurityProperties props, DemoAccountGuard demoGuard) {
        this.userRepo = userRepository;
        this.props = props;
        this.demoGuard = demoGuard;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String email) {
        userRepo.findByEmail(EmailNormalizer.normalize(email)).ifPresent(user -> {
            if (demoGuard.isProtected(user)) {
                return;
            }
            int attempts = user.getFailedLoginAttempts() + 1;
            if (attempts >= props.maxFailedAttempts()) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(props.lockMinutes()));
                user.setFailedLoginAttempts(0);
                log.warn("Account temporarily locked after repeated failures, userId={}", user.getId());
            } else {
                user.setFailedLoginAttempts(attempts);
            }
        });
    }

    @Override
    @Transactional
    public void recordSuccess(String email) {
        userRepo.findByEmail(EmailNormalizer.normalize(email)).ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
            }
        });
    }
}
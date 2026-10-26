package com.mdev.campusgate.service.impl;

import com.mdev.campusgate.configuration.JwtProperties;
import com.mdev.campusgate.dto.request.LoginRequestDTO;
import com.mdev.campusgate.dto.response.LoginResponseDTO;
import com.mdev.campusgate.security.JwtTokenService;
import com.mdev.campusgate.service.ILoginAttemptService;
import com.mdev.campusgate.service.ILoginService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements ILoginService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final ILoginAttemptService loginAttemptServ;

    public LoginService(JwtTokenService jwtTokenService,
                        JwtProperties jwtProperties,
                        AuthenticationManager authenticationManager,
                        ILoginAttemptService loginAttemptService){
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.authenticationManager = authenticationManager;
        this.loginAttemptServ = loginAttemptService;
    }

    @Override
    public LoginResponseDTO userLogin(LoginRequestDTO dto) {
        Authentication result;
        try {
            result = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(dto.email(), dto.password()));
        } catch (BadCredentialsException ex) {
            loginAttemptServ.recordFailure(dto.email());
            throw ex;
        }

        loginAttemptServ.recordSuccess(result.getName());
        String token = jwtTokenService.generateToken(result);

        return new LoginResponseDTO(
                token,
                "Bearer",
                jwtProperties.expirationSeconds()
        );
    }
}
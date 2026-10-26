package com.mdev.campusgate.controller;

import com.mdev.campusgate.dto.request.LoginRequestDTO;
import com.mdev.campusgate.dto.request.UpdatePasswordRequestDTO;
import com.mdev.campusgate.dto.response.LoginResponseDTO;
import com.mdev.campusgate.dto.response.MeResponseDTO;
import com.mdev.campusgate.security.JwtTokenService;
import com.mdev.campusgate.service.ILoginService;
import com.mdev.campusgate.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final ILoginService loginServ;
    private final IUserService userServ;

    public AuthController(ILoginService loginService, IUserService userService){
        this.loginServ = loginService;
        this.userServ = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> userLogin(@Valid @RequestBody LoginRequestDTO dto){
        return ResponseEntity.ok(loginServ.userLogin(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> getUserLoggedInfo(@AuthenticationPrincipal Jwt jwt){
        MeResponseDTO dto = new MeResponseDTO(
                jwt.getSubject(),
                jwt.getClaimAsStringList(JwtTokenService.PERMISSIONS_CLAIM)
        );
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequestDTO dto,
                                               @AuthenticationPrincipal Jwt jwt){
        userServ.updatePassword(UUID.fromString(jwt.getSubject()), dto);
        return ResponseEntity.noContent().build();
    }
}
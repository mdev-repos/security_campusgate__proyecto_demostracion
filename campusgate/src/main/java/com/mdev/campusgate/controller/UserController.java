package com.mdev.campusgate.controller;

import com.mdev.campusgate.dto.request.CreateUserRequestDTO;
import com.mdev.campusgate.dto.request.UpdateUserRequestDTO;
import com.mdev.campusgate.dto.response.PageResponseDTO;
import com.mdev.campusgate.dto.response.UserResponseDTO;
import com.mdev.campusgate.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    private final IUserService userServ;

    public UserController(IUserService userService){
        this.userServ = userService;
    }

    @PreAuthorize("hasAuthority('USER_CREATE')")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO dto){
        UserResponseDTO created = userServ.createUser(dto);
        URI location = URI.create("/users/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable UUID id){
        return ResponseEntity.ok(userServ.getUserById(id));
    }

    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public ResponseEntity<PageResponseDTO<UserResponseDTO>> getUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable){
        return ResponseEntity.ok(userServ.getAllUsers(pageable));
    }

    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateUserRequestDTO dto,
                                                      @AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(userServ.updateUser(id, dto, UUID.fromString(jwt.getSubject())));
    }

    @PreAuthorize("hasAuthority('USER_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableUser(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt){
        userServ.disableUser(id, UUID.fromString(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }
}
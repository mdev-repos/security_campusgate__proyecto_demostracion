package com.mdev.campusgate.controller;

import com.mdev.campusgate.dto.request.CreateProfileRequestDTO;
import com.mdev.campusgate.dto.response.ProfileResponseDTO;
import com.mdev.campusgate.service.IProfessorService;
import jakarta.validation.Valid;
import com.mdev.campusgate.dto.response.PageResponseDTO;
import com.mdev.campusgate.dto.response.CourseResponseDTO;
import com.mdev.campusgate.service.ICourseService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/professors")
public class ProfessorController {
    private final IProfessorService professorServ;
    private final ICourseService courseServ;

    public ProfessorController(IProfessorService professorService, ICourseService courseService) {
        this.professorServ = professorService;
        this.courseServ = courseService;
    }

    @PreAuthorize("hasAuthority('PROFESSOR_CREATE')")
    @PostMapping
    public ResponseEntity<ProfileResponseDTO> createProfessor(@Valid @RequestBody CreateProfileRequestDTO dto) {
        ProfileResponseDTO created = professorServ.createProfessor(dto);
        return ResponseEntity.created(URI.create("/professors/" + created.id())).body(created);
    }

    @PreAuthorize("hasAuthority('PROFESSOR_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponseDTO> getProfessor(@PathVariable UUID id) {
        return ResponseEntity.ok(professorServ.getProfessorById(id));
    }

    @PreAuthorize("hasAuthority('PROFESSOR_READ')")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDTO> getMyProfessorProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(professorServ.getProfessorByUserId(UUID.fromString(jwt.getSubject())));
    }

    @PreAuthorize("hasAuthority('PROFESSOR_READ')")
    @GetMapping("/me/courses")
    public ResponseEntity<PageResponseDTO<CourseResponseDTO>> getMyCourses(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(courseServ.getCoursesByProfessorUser(userId, pageable));
    }

    @PreAuthorize("hasAuthority('PROFESSOR_READ_ANY')")
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProfileResponseDTO>> getAllProfessors(
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        return ResponseEntity.ok(professorServ.getAllProfessors(pageable));
    }
}

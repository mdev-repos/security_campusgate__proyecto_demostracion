package com.mdev.campusgate.controller;

import com.mdev.campusgate.dto.request.CreateProfileRequestDTO;
import com.mdev.campusgate.dto.response.ProfileResponseDTO;
import com.mdev.campusgate.service.IStudentService;
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
@RequestMapping("/students")
public class StudentController {
    private final IStudentService studentServ;
    private final ICourseService courseServ;

    public StudentController(IStudentService studentService, ICourseService courseService) {
        this.studentServ = studentService;
        this.courseServ = courseService;
    }

    @PreAuthorize("hasAuthority('STUDENT_CREATE')")
    @PostMapping
    public ResponseEntity<ProfileResponseDTO> createStudent(@Valid @RequestBody CreateProfileRequestDTO dto) {
        ProfileResponseDTO created = studentServ.createStudent(dto);
        return ResponseEntity.created(URI.create("/students/" + created.id())).body(created);
    }

    @PreAuthorize("hasAuthority('STUDENT_READ_ANY') or (hasAuthority('STUDENT_READ') and @studentAuth.isSelf(#id, authentication))")
    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponseDTO> getStudent(@PathVariable UUID id) {
        return ResponseEntity.ok(studentServ.getStudentById(id));
    }

    @PreAuthorize("hasAuthority('STUDENT_READ')")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDTO> getMyStudentProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(studentServ.getStudentByUserId(UUID.fromString(jwt.getSubject())));
    }

    @PreAuthorize("hasAuthority('STUDENT_READ')")
    @GetMapping("/me/courses")
    public ResponseEntity<PageResponseDTO<CourseResponseDTO>> getMyCourses(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(courseServ.getCoursesByStudentUser(userId, pageable));
    }

    @PreAuthorize("hasAuthority('STUDENT_READ_ANY')")
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProfileResponseDTO>> getAllStudents(
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        return ResponseEntity.ok(studentServ.getAllStudents(pageable));
    }
}

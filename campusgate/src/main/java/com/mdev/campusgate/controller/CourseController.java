package com.mdev.campusgate.controller;

import com.mdev.campusgate.dto.request.CreateCourseRequestDTO;
import com.mdev.campusgate.dto.request.UpdateCourseRequestDTO;
import com.mdev.campusgate.dto.response.CourseResponseDTO;
import com.mdev.campusgate.dto.response.PageResponseDTO;
import com.mdev.campusgate.dto.response.ProfileResponseDTO;
import com.mdev.campusgate.service.ICourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
public class CourseController {
    private final ICourseService courseServ;

    public CourseController(ICourseService courseService) {
        this.courseServ = courseService;
    }

    @PreAuthorize("hasAuthority('COURSE_CREATE')")
    @PostMapping
    public ResponseEntity<CourseResponseDTO> createCourse(@Valid @RequestBody CreateCourseRequestDTO dto) {
        CourseResponseDTO created = courseServ.createCourse(dto);
        return ResponseEntity.created(URI.create("/courses/" + created.id())).body(created);
    }

    @PreAuthorize("hasAuthority('COURSE_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> getCourse(@PathVariable UUID id) {
        return ResponseEntity.ok(courseServ.getCourseById(id));
    }

    @PreAuthorize("hasAuthority('COURSE_READ')")
    @GetMapping
    public ResponseEntity<PageResponseDTO<CourseResponseDTO>> getCourses(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(courseServ.getAllCourses(pageable));
    }

    @PreAuthorize("hasAuthority('COURSE_UPDATE_ANY') or (hasAuthority('COURSE_UPDATE') and @courseAuth.isOwner(#id, authentication))")
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> updateCourse(@PathVariable UUID id,
                                                          @Valid @RequestBody UpdateCourseRequestDTO dto) {
        return ResponseEntity.ok(courseServ.updateCourse(id, dto));
    }

    @PreAuthorize("hasAuthority('COURSE_UPDATE_ANY')")
    @PostMapping("/{courseId}/students/{studentId}")
    public ResponseEntity<Void> enrollStudent(@PathVariable UUID courseId, @PathVariable UUID studentId) {
        courseServ.enrollStudent(courseId, studentId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('COURSE_UPDATE_ANY') or (hasAuthority('COURSE_UPDATE') and @courseAuth.isOwner(#id, authentication))")
    @GetMapping("/{id}/students")
    public ResponseEntity<PageResponseDTO<ProfileResponseDTO>> getCourseStudents(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        return ResponseEntity.ok(courseServ.getCourseStudents(id, pageable));
    }

    @PreAuthorize("hasAuthority('COURSE_UPDATE_ANY')")
    @DeleteMapping("/{courseId}/students/{studentId}")
    public ResponseEntity<Void> unenrollStudent(@PathVariable UUID courseId, @PathVariable UUID studentId) {
        courseServ.unenrollStudent(courseId, studentId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('COURSE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseServ.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}

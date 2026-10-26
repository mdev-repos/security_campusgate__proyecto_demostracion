package com.mdev.campusgate.service.impl;

import com.mdev.campusgate.dto.request.CreateCourseRequestDTO;
import com.mdev.campusgate.dto.request.UpdateCourseRequestDTO;
import com.mdev.campusgate.dto.response.CourseResponseDTO;
import com.mdev.campusgate.dto.response.PageResponseDTO;
import com.mdev.campusgate.exception.BusinessRuleException;
import com.mdev.campusgate.exception.ResourceNotFoundException;
import com.mdev.campusgate.dto.response.ProfileResponseDTO;
import com.mdev.campusgate.mapper.CourseMapper;
import com.mdev.campusgate.mapper.ProfileMapper;
import com.mdev.campusgate.model.Course;
import com.mdev.campusgate.model.Professor;
import com.mdev.campusgate.model.Student;
import com.mdev.campusgate.repository.ICourseRepository;
import com.mdev.campusgate.repository.IProfessorRepository;
import com.mdev.campusgate.repository.IStudentRepository;
import com.mdev.campusgate.service.ICourseService;
import com.mdev.campusgate.utils.PageableValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class CourseService implements ICourseService {
    private static final Set<String> ALLOWED_SORT = Set.of("title", "createdAt");
    private static final Set<String> ALLOWED_STUDENT_SORT = Set.of("firstName", "lastName", "createdAt");

    private final ICourseRepository courseRepo;
    private final IProfessorRepository professorRepo;
    private final IStudentRepository studentRepo;

    public CourseService(ICourseRepository courseRepository,
                         IProfessorRepository professorRepository,
                         IStudentRepository studentRepository) {
        this.courseRepo = courseRepository;
        this.professorRepo = professorRepository;
        this.studentRepo = studentRepository;
    }

    @Override
    @Transactional
    public CourseResponseDTO createCourse(CreateCourseRequestDTO dto) {
        Professor professor = professorRepo.findById(dto.professorId())
                .orElseThrow(() -> new ResourceNotFoundException("Professor"));

        Course course = new Course();
        course.setTitle(dto.title());
        course.setDescription(dto.description());
        course.setProfessor(professor);

        Course saved = courseRepo.save(course);
        log.info("Course created with id {}", saved.getId());
        return CourseMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDTO getCourseById(UUID id) {
        return CourseMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CourseResponseDTO> getAllCourses(Pageable pageable) {
        PageableValidator.validateSort(pageable, ALLOWED_SORT);
        Page<CourseResponseDTO> page = courseRepo.findAll(pageable).map(CourseMapper::toResponse);
        return PageResponseDTO.from(page);
    }

    @Override
    @Transactional
    public CourseResponseDTO updateCourse(UUID id, UpdateCourseRequestDTO dto) {
        Course course = findOrThrow(id);
        course.setTitle(dto.title());
        course.setDescription(dto.description());
        log.info("Course {} updated", id);
        return CourseMapper.toResponse(course);
    }

    @Override
    @Transactional
    public void enrollStudent(UUID courseId, UUID studentId) {
        Course course = findOrThrow(courseId);
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student"));

        if (!course.getStudents().add(student)) {
            throw new BusinessRuleException("Student is already enrolled in this course");
        }
        log.info("Student {} enrolled in course {}", studentId, courseId);
    }

    private Course findOrThrow(UUID id) {
        return courseRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course"));
    }

    @Override
    @Transactional
    public void unenrollStudent(UUID courseId, UUID studentId) {
        Course course = findOrThrow(courseId);
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student"));

        if (!course.getStudents().remove(student)) {
            throw new ResourceNotFoundException("Enrollment");
        }
        log.info("Student {} removed from course {}", studentId, courseId);
    }

    @Override
    @Transactional
    public void deleteCourse(UUID id) {
        courseRepo.delete(findOrThrow(id));
        log.info("Course {} deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CourseResponseDTO> getCoursesByProfessorUser(UUID userId, Pageable pageable) {
        PageableValidator.validateSort(pageable, ALLOWED_SORT);
        return PageResponseDTO.from(courseRepo.findByProfessorUserId(userId, pageable).map(CourseMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CourseResponseDTO> getCoursesByStudentUser(UUID userId, Pageable pageable) {
        PageableValidator.validateSort(pageable, ALLOWED_SORT);
        return PageResponseDTO.from(courseRepo.findByStudentsUserId(userId, pageable).map(CourseMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProfileResponseDTO> getCourseStudents(UUID courseId, Pageable pageable) {
        PageableValidator.validateSort(pageable, ALLOWED_STUDENT_SORT);
        if (!courseRepo.existsById(courseId)) {
            throw new ResourceNotFoundException("Course");
        }
        return PageResponseDTO.from(studentRepo.findByCourseId(courseId, pageable).map(ProfileMapper::toResponse));
    }
}

package com.mdev.campusgate.configuration;

import com.mdev.campusgate.model.Course;
import com.mdev.campusgate.model.Professor;
import com.mdev.campusgate.model.Student;
import com.mdev.campusgate.model.UserSec;
import com.mdev.campusgate.model.enums.Role;
import com.mdev.campusgate.repository.ICourseRepository;
import com.mdev.campusgate.repository.IProfessorRepository;
import com.mdev.campusgate.repository.IStudentRepository;
import com.mdev.campusgate.repository.IUserRepository;
import com.mdev.campusgate.security.DemoAccountGuard;
import com.mdev.campusgate.utils.EmailNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

// Public demo only (app.demo.enabled=true). Wipes every user except the real admin and loads fictional data.
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.demo", name = "enabled", havingValue = "true")
public class DemoDataService implements ApplicationRunner {
    private static final List<String> FIRST_NAMES = List.of(
            "Lucía", "Martín", "Camila", "Joaquín", "Valentina", "Mateo", "Sofía", "Tomás", "Agustina", "Nicolás",
            "Florencia", "Santiago", "Julieta", "Facundo", "Milagros", "Franco", "Carolina", "Lautaro", "Micaela", "Ignacio");
    private static final List<String> LAST_NAMES = List.of(
            "Fernández", "Gómez", "Rodríguez", "López", "Martínez", "Sosa", "Romero", "Díaz", "Torres", "Álvarez",
            "Acosta", "Benítez", "Herrera", "Medina", "Castro", "Suárez", "Ríos", "Molina", "Ortiz", "Silva");
    private static final List<String[]> COURSES = List.of(
            new String[]{"Introducción a la Programación", "Variables, estructuras de control y funciones con ejercicios prácticos."},
            new String[]{"Bases de Datos Relacionales", "Modelado, SQL y normalización sobre casos reales."},
            new String[]{"Estadística Aplicada", "Probabilidad y análisis de datos para la toma de decisiones."},
            new String[]{"Redes de Computadoras", "Modelo TCP/IP, direccionamiento y servicios de red."},
            new String[]{"Diseño de Interfaces", "Principios de usabilidad, accesibilidad y prototipado."},
            new String[]{"Álgebra Lineal", "Matrices, sistemas de ecuaciones y espacios vectoriales."},
            new String[]{"Historia del Arte Contemporáneo", "Movimientos artísticos del siglo XX y su contexto social."},
            new String[]{"Química Orgánica I", "Estructura, nomenclatura y reactividad de compuestos orgánicos."},
            new String[]{"Literatura Latinoamericana", "Lectura crítica de autores y corrientes del continente."},
            new String[]{"Economía para No Economistas", "Conceptos básicos de mercados, precios e inflación."},
            new String[]{"Física General", "Mecánica, energía y ondas con laboratorio virtual."},
            new String[]{"Inglés Técnico", "Comprensión de textos y vocabulario del ámbito profesional."});
    private static final int PROFESSORS = 8;
    private static final int STUDENTS = 40;

    private final IUserRepository userRepo;
    private final IProfessorRepository professorRepo;
    private final IStudentRepository studentRepo;
    private final ICourseRepository courseRepo;
    private final PasswordEncoder passwordEncoder;
    private final DemoProperties demoProperties;
    private final AdminProperties adminProperties;
    private final TransactionTemplate tx;

    public DemoDataService(IUserRepository userRepository,
                           IProfessorRepository professorRepository,
                           IStudentRepository studentRepository,
                           ICourseRepository courseRepository,
                           PasswordEncoder passwordEncoder,
                           DemoProperties demoProperties,
                           AdminProperties adminProperties,
                           TransactionTemplate transactionTemplate) {
        this.userRepo = userRepository;
        this.professorRepo = professorRepository;
        this.studentRepo = studentRepository;
        this.courseRepo = courseRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoProperties = demoProperties;
        this.adminProperties = adminProperties;
        this.tx = transactionTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (demoProperties.password() == null || demoProperties.password().length() < 12) {
            throw new IllegalStateException("app.demo.password (DEMO_PASSWORD) must be set with at least 12 characters");
        }
        reset();
    }

    @Scheduled(cron = "${app.demo.reset-cron:0 0 4 * * *}", zone = "America/Argentina/Buenos_Aires")
    public void scheduledReset() {
        reset();
    }

    public void reset() {
        tx.executeWithoutResult(status -> {
            wipe();
            seed();
        });
        log.info("Demo data reset");
    }

    private void wipe() {
        String adminEmail = EmailNormalizer.normalize(adminProperties.email());
        courseRepo.deleteAll();
        studentRepo.deleteAll();
        professorRepo.deleteAll();
        userRepo.findAll().stream()
                .filter(u -> !u.getEmail().equals(adminEmail))
                .forEach(userRepo::delete);
        userRepo.flush();
    }

    private void seed() {
        String demoHash = passwordEncoder.encode(demoProperties.password());
        // Seeded users that nobody logs in with get a random password, hashed once and reused.
        String unusableHash = passwordEncoder.encode(UUID.randomUUID().toString());
        Set<String> usedEmails = new HashSet<>();

        createUser("admin@" + DemoAccountGuard.DEMO_DOMAIN, demoHash, Role.ADMIN);

        List<Professor> professors = new ArrayList<>();
        for (int i = 0; i < PROFESSORS; i++) {
            String first = FIRST_NAMES.get(i);
            String last = LAST_NAMES.get((i * 3 + 1) % LAST_NAMES.size());
            String email = i == 0 ? "profesor@" + DemoAccountGuard.DEMO_DOMAIN : uniqueEmail(first, last, usedEmails);
            UserSec user = createUser(email, i == 0 ? demoHash : unusableHash, Role.PROFESSOR);
            Professor p = new Professor();
            p.setUser(user);
            p.setFirstName(first);
            p.setLastName(last);
            professors.add(professorRepo.save(p));
        }

        List<Student> students = new ArrayList<>();
        for (int i = 0; i < STUDENTS; i++) {
            String first = FIRST_NAMES.get((i + 5) % FIRST_NAMES.size());
            // The shift every 20 students keeps first + last name pairs unique.
            String last = LAST_NAMES.get((i * 7 + 2 + (i / FIRST_NAMES.size()) * 3) % LAST_NAMES.size());
            String email = i == 0 ? "alumno@" + DemoAccountGuard.DEMO_DOMAIN : uniqueEmail(first, last, usedEmails);
            UserSec user = createUser(email, i == 0 ? demoHash : unusableHash, Role.STUDENT);
            Student s = new Student();
            s.setUser(user);
            s.setFirstName(first);
            s.setLastName(last);
            students.add(studentRepo.save(s));
        }

        // The demo professor owns the first three courses; the rest are spread over the other professors.
        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < COURSES.size(); i++) {
            Course c = new Course();
            c.setTitle(COURSES.get(i)[0]);
            c.setDescription(COURSES.get(i)[1]);
            c.setProfessor(i < 3 ? professors.get(0) : professors.get(1 + (i - 3) % (PROFESSORS - 1)));
            courses.add(c);
        }

        // The demo student is enrolled in courses of the demo professor and of other professors.
        for (int idx : new int[]{0, 1, 3, 5}) {
            courses.get(idx).getStudents().add(students.get(0));
        }
        Random random = new Random(42);
        for (int i = 1; i < students.size(); i++) {
            int enrollments = 3 + random.nextInt(3);
            Set<Integer> chosen = new HashSet<>();
            while (chosen.size() < enrollments) {
                chosen.add(random.nextInt(courses.size()));
            }
            for (int idx : chosen) {
                courses.get(idx).getStudents().add(students.get(i));
            }
        }
        courseRepo.saveAll(courses);
    }

    private UserSec createUser(String email, String passwordHash, Role role) {
        UserSec user = new UserSec();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setEnabled(true);
        user.setAccountNotLocked(true);
        user.getRoles().add(role);
        return userRepo.save(user);
    }

    private String uniqueEmail(String first, String last, Set<String> used) {
        String base = ascii(first) + "." + ascii(last);
        String email = base + "@" + DemoAccountGuard.DEMO_DOMAIN;
        int n = 2;
        while (!used.add(email)) {
            email = base + n++ + "@" + DemoAccountGuard.DEMO_DOMAIN;
        }
        return email;
    }

    private String ascii(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }
}

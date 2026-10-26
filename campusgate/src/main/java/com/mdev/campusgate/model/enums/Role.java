package com.mdev.campusgate.model.enums;

import lombok.Getter;

import java.util.Set;

@Getter
public enum Role {
    ADMIN(Permission.values()),
    PROFESSOR(
            Permission.COURSE_READ,
            Permission.COURSE_UPDATE,
            Permission.STUDENT_READ,
            Permission.PROFESSOR_READ),
    STUDENT(
            Permission.COURSE_READ,
            Permission.STUDENT_READ
    );

    private final Set<Permission> permissions;

    Role(Permission... permissions) {
        this.permissions = Set.of(permissions);
    }
}
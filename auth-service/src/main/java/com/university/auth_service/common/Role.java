package com.university.auth_service.common;

public enum Role {
    ADMIN("Full access"),
    PRUEFUNGSAMT("User management"),
    LEHRENDER("Teaching"),
    STUDENT("Student");

    Role(String description) {
    }
}

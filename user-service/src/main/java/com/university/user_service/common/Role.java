package com.university.user_service.common;

public enum Role {
    ADMIN("Vollzugriff auf alle IAM-Funktionen"),
    PRUEFUNGSAMT("Userverwaltung, aber kein IAM-Admin"),
    LEHRENDER("Keine IAM-Berechtigungen, nur JWT für Notenservice"),
    STUDENT("Keine IAM-Berechtigungen, nur JWT für Notenservice");

    Role(String description) {
    }
}

package de.frauas.notenmodulservice.model;

/**
 * Status einer Note im System
 * - ADD: Neu angelegt vom Prüfungsamt, noch keine Note eingetragen
 * - TO_VALIDATE: Von Lehrendem eingetragen, wartet auf Validierung
 * - PUBLISHED: Vom Prüfungsamt validiert und für Studierende sichtbar
 */
public enum NotenStatus {
    ADD,
    TO_VALIDATE,
    PUBLISHED
}
package com.university.notenberechnung_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response-Wrapper für die GraphQL-Abfrage {@code notenVonStudent}.
 *
 * <p>Bildet die JSON-Antwort des Noten-Modul-Service ab. Die Struktur folgt dem
 * GraphQL-Standard: {@code { "data": { "notenVonStudent": [...] } }}.
 *
 * <p>Enthält zwei innere Klassen:
 * <ul>
 *   <li>{@link DataWrapper} – kapselt das {@code data}-Objekt der GraphQL-Antwort</li>
 *   <li>{@link GraphQLNote} – repräsentiert eine einzelne Note mit allen Feldern</li>
 * </ul>
 *
 * @see com.university.notenberechnung_service.service.GraphQLClientService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphQLNoteResponse {
    private DataWrapper data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataWrapper {
        @JsonProperty("notenVonStudent")
        private List<GraphQLNote> notenVonStudent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphQLNote {
        private String id;
        private String matrikelnummer;
        private String tenantId;
        private Integer modulId;
        private String modulName;
        private Double note;
        private String status;
        private String erstelltAm;
        private String aktualisiertAm;
    }
}

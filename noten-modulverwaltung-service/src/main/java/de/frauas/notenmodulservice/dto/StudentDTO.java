package de.frauas.notenmodulservice.dto;

import de.frauas.notenmodulservice.model.Note;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO für Studenteninformationen mit ihren Noten
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {

    private String matrikelnummer;
    private String firstName;
    private String lastName;
    private String studiengang;
    private Integer semester;
    private List<Note> noten;

    /**
     * Gibt die Anzahl der bestandenen Module zurück
     */
    public long getAnzahlBestandeneModule() {
        if (noten == null) {
            return 0;
        }

        return noten.stream()
                .filter(Note::isBestanden)
                .count();
    }
}
package de.frauas.notenmodulservice.exception;

/**
 * Exception für nicht gefundene Ressourcen.
 * 
 * <p>Wird in GraphQL auf {@code ErrorType.NOT_FOUND} gemappt.</p>
 * 
 * <p>Verwendung:</p>
 * <ul>
 *   <li>Note mit ID nicht gefunden</li>
 *   <li>Modul mit ID nicht gefunden</li>
 *   <li>Student mit Matrikelnummer nicht gefunden</li>
 * </ul>
 */
public class NotFoundException extends RuntimeException {
    
    public NotFoundException(String message) {
        super(message);
    }
    
    /**
     * Convenience-Konstruktor für einheitliche Fehlermeldungen.
     * 
     * @param resourceType Typ der Ressource (z.B. "Note", "Modul")
     * @param id Die ID der nicht gefundenen Ressource
     */
    public NotFoundException(String resourceType, Object id) {
        super(resourceType + " mit ID " + id + " nicht gefunden");
    }
}

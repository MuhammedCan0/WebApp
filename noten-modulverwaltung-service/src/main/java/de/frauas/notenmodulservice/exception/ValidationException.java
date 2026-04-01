package de.frauas.notenmodulservice.exception;

/**
 * Exception für Validierungsfehler bei Eingabedaten.
 * 
 * <p>Wird in GraphQL auf {@code ErrorType.BAD_REQUEST} gemappt.</p>
 * 
 * <p>Verwendung:</p>
 * <ul>
 *   <li>Pflichtfelder fehlen (z.B. tenant-IDs)</li>
 *   <li>Werte außerhalb gültiger Bereiche (z.B. Note &lt; 1.0)</li>
 *   <li>Formatfehler bei Eingaben</li>
 * </ul>
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
}

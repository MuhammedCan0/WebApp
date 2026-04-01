package de.frauas.notenmodulservice.exception;

/**
 * Exception für Zugriffsverweigerungen bei fehlenden Berechtigungen.
 * 
 * <p>Wird in GraphQL auf {@code ErrorType.FORBIDDEN} gemappt.</p>
 * 
 * <p>Verwendung:</p>
 * <ul>
 *   <li>Benutzer hat nicht die erforderliche Rolle</li>
 *   <li>Zugriff auf fremde Ressourcen (z.B. Student auf fremde Noten)</li>
 *   <li>Tenant-übergreifende Zugriffe ohne Berechtigung</li>
 * </ul>
 */
public class AccessDeniedException extends RuntimeException {
    
    public AccessDeniedException(String message) {
        super(message);
    }
    
    public AccessDeniedException() {
        super("Zugriff verweigert");
    }
}

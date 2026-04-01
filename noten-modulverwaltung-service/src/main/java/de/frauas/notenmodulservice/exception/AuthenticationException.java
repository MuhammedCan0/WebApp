package de.frauas.notenmodulservice.exception;

/**
 * Exception für fehlende oder ungültige Authentifizierung.
 * 
 * <p>Wird in GraphQL auf {@code ErrorType.UNAUTHORIZED} gemappt.</p>
 * 
 * <p>Verwendung:</p>
 * <ul>
 *   <li>JWT fehlt im Request</li>
 *   <li>JWT ist abgelaufen oder ungültig</li>
 *   <li>Authentifizierung konnte nicht durchgeführt werden</li>
 * </ul>
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException() {
        super("Nicht authentifiziert: Bitte melden Sie sich an");
    }
}

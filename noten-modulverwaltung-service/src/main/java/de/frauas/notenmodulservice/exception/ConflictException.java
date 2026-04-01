package de.frauas.notenmodulservice.exception;

/**
 * Exception für Konflikte bei Ressourcen-Operationen.
 * 
 * <p>Wird in GraphQL auf {@code ErrorType.BAD_REQUEST} mit Extension-Code "CONFLICT" gemappt.</p>
 * 
 * <p>Verwendung:</p>
 * <ul>
 *   <li>Ressource mit gleicher ID existiert bereits</li>
 *   <li>Duplikate (z.B. Note für Student+Modul bereits vorhanden)</li>
 * </ul>
 */
public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
}

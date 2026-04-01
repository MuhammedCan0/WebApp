package de.frauas.notenmodulservice.exception;

/**
 * Exception für ungültige Zustände bei Ressourcen-Operationen.
 * 
 * <p>Wird in GraphQL auf {@code ErrorType.BAD_REQUEST} mit Extension-Code "INVALID_STATE" gemappt.</p>
 * 
 * <p>Verwendung:</p>
 * <ul>
 *   <li>Note kann nicht geändert werden (bereits PUBLISHED)</li>
 *   <li>Note kann nicht validiert werden (nicht im Status TO_VALIDATE)</li>
 *   <li>Ungültige Zustandsübergänge im Workflow</li>
 * </ul>
 */
public class InvalidStateException extends RuntimeException {
    
    public InvalidStateException(String message) {
        super(message);
    }
}

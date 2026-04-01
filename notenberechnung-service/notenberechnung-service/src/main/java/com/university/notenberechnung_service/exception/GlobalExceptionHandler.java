package com.university.notenberechnung_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Globaler Exception Handler für den Notenberechnung-Service.
 * 
 * <p>Fängt alle Exceptions ab und gibt einheitliche JSON-Fehlerantworten zurück.
 * Dies verbessert die API-Konsistenz und erleichtert das Debugging.
 * 
 * <p>Behandelte Exception-Typen:
 * <ul>
 *   <li>{@link ResponseStatusException} - Spring Web Exceptions mit HTTP-Status</li>
 *   <li>{@link IllegalArgumentException} - Ungültige Argumente (400 Bad Request)</li>
 *   <li>{@link Exception} - Alle anderen Exceptions (500 Internal Server Error)</li>
 * </ul>
 * 
 * @author Notenberechnung-Service Team
 * @since 1.0
 * @see com.university.notenberechnung_service.controller.NotenController
 * @see com.university.notenberechnung_service.controller.StudentGradesController
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Behandelt ResponseStatusException von Spring Web.
     * Gibt den entsprechenden HTTP-Status und die Fehlermeldung zurück.
     *
     * @param ex die ausgelöste ResponseStatusException
     * @return ResponseEntity mit Fehlerdetails
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("ResponseStatusException: {} - {}", ex.getStatusCode(), ex.getReason());

        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        Map<String, Object> body = createErrorBody(
                ex.getStatusCode().value(),
                ex.getStatusCode().toString(),
                message
        );

        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    /**
     * Behandelt IllegalArgumentException.
     * Wird als 400 Bad Request zurückgegeben.
     *
     * @param ex die ausgelöste IllegalArgumentException
     * @return ResponseEntity mit Fehlerdetails
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage()
        );
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Behandelt Bean Validation Fehler (z.B. @NotNull, @Min, @Max).
     * Sammelt alle Validierungsfehler und gibt sie als Liste zurück.
     *
     * @param ex die ausgelöste MethodArgumentNotValidException
     * @return ResponseEntity mit Fehlerdetails
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        
        log.warn("Validierungsfehler: {}", errors);
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validierungsfehler: " + String.join(", ", errors)
        );
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Behandelt NullPointerException.
     * Wird als 400 Bad Request zurückgegeben mit hilfreicher Meldung.
     *
     * @param ex die ausgelöste NullPointerException
     * @return ResponseEntity mit Fehlerdetails
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointerException(NullPointerException ex) {
        log.error("NullPointerException: {}", ex.getMessage(), ex);
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Ein erforderlicher Wert fehlt. Bitte überprüfen Sie Ihre Eingabe."
        );
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Fallback-Handler für alle anderen Exceptions.
     * Wird als 500 Internal Server Error zurückgegeben.
     *
     * @param ex die ausgelöste Exception
     * @return ResponseEntity mit Fehlerdetails
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unerwarteter Fehler: {}", ex.getMessage(), ex);
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ein interner Fehler ist aufgetreten. Bitte versuchen Sie es später erneut."
        );
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Erstellt den einheitlichen Fehler-Response-Body.
     *
     * @param status HTTP-Statuscode
     * @param error HTTP-Statusbeschreibung
     * @param message Detaillierte Fehlermeldung
     * @return Map mit Fehlerdetails
     */
    private Map<String, Object> createErrorBody(int status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("service", "notenberechnung-service");
        return body;
    }
}

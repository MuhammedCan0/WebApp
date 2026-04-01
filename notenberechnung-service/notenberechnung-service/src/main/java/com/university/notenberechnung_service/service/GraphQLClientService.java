package com.university.notenberechnung_service.service;

import com.university.notenberechnung_service.dto.GraphQLNoteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GraphQL Client Service für die Integration mit dem Noten-Modul-Service.
 * 
 * <p>Verantwortlich für:
 * <ul>
 *   <li>Abruf von Studentennoten via GraphQL Query</li>
 *   <li>Weitergabe des JWT-Tokens für Authentifizierung</li>
 *   <li>Reactive/Non-blocking HTTP-Kommunikation mit WebClient</li>
 *   <li>Fehlerbehandlung bei Netzwerk- und GraphQL-Fehlern</li>
 * </ul>
 * 
 * <p>Technische Details:
 * <ul>
 *   <li>Verwendet Spring WebFlux WebClient für non-blocking I/O</li>
 *   <li>100% reactive - keine .block() Aufrufe</li>
 *   <li>GraphQL Query: {@code notenVonStudent(matrikelnummer)}</li>
 *   <li>URL konfigurierbar via {@code graphql.client.url} Property</li>
 * </ul>
 * 
 * <p>Fehlerbehandlung:
 * <ul>
 *   <li>WebClientResponseException: HTTP-Fehler vom Noten-Modul-Service</li>
 *   <li>Timeout: Netzwerk-Probleme werden geloggt</li>
 *   <li>Leere Response: Gibt leere Liste zurück</li>
 * </ul>
 * 
 * @see NotenService
 * @see GraphQLNoteResponse
 */
@Service
@Slf4j
public class GraphQLClientService {

    /** URL zum GraphQL-Endpoint des Noten-Modul-Service */
    @Value("${graphql.client.url}")
    private String graphqlUrl;

    private final WebClient webClient;

    public GraphQLClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Fragt die Noten eines Studierenden vom Noten-Modul-Service via GraphQL ab (reaktive Version).
     * Übermittelt den JWT-Token als Authorization Header an den Noten-Modul-Service.
     * WICHTIG: Keine blockierenden Operationen – nutzt Mono für asynchrone Verarbeitung.
     *
     * @param matrikelnummer Matrikelnummer des Studierenden
     * @param authorizationHeader JWT Authorization Header (z.B. "Bearer token...")
     * @return Mono mit Liste der Noten oder leere Liste bei fehlendem Token/Fehler
     */
    public Mono<List<GraphQLNoteResponse.GraphQLNote>> getNotenVonStudent(
            String matrikelnummer,
            String authorizationHeader) {

        String query = """
                query {
                    notenVonStudent(matrikelnummer: "%s") {
                        id
                        matrikelnummer
                        tenantId
                        modulId
                        modulName
                        note
                        status
                        erstelltAm
                        aktualisiertAm
                    }
                }
                """.formatted(matrikelnummer);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", query);

        log.debug("Executing GraphQL query for student: {}", matrikelnummer);

        // Baue WebClient Request mit JWT Header
        var requestSpec = webClient.post()
                .uri(graphqlUrl)
                .contentType(MediaType.APPLICATION_JSON);

        // Wenn Authorization Header vorhanden, füge ihn hinzu
        if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
            requestSpec = requestSpec.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        // Vollständig reactive Chain - KEIN .block()!
        return requestSpec
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GraphQLNoteResponse.class)
                .flatMap(response -> {
                    try {
                        if (response == null || response.getData() == null) {
                            log.warn("No data in GraphQL response for student: {}", matrikelnummer);
                            return Mono.just(List.<GraphQLNoteResponse.GraphQLNote>of());
                        }

                        if (response.getData().getNotenVonStudent() == null) {
                            log.debug("No notes found for student: {}", matrikelnummer);
                            return Mono.just(List.<GraphQLNoteResponse.GraphQLNote>of());
                        }

                        // Filtere null-Noten heraus
                        List<GraphQLNoteResponse.GraphQLNote> notes = response.getData().getNotenVonStudent()
                                .stream()
                                .filter(note -> note.getNote() != null && !Double.isNaN(note.getNote()))
                                .toList();

                        log.debug("Successfully retrieved {} notes for student: {} (gefiltert: {} Noten mit null-Wert)", 
                                response.getData().getNotenVonStudent().size(), matrikelnummer,
                                response.getData().getNotenVonStudent().size() - notes.size());
                        return Mono.just(notes);
                    } catch (Exception e) {
                        return Mono.just(List.<GraphQLNoteResponse.GraphQLNote>of());
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("GraphQL query failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
                    return Mono.just(List.<GraphQLNoteResponse.GraphQLNote>of());
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Error executing GraphQL query: {}", e.getMessage(), e);
                    return Mono.just(List.<GraphQLNoteResponse.GraphQLNote>of());
                });
    }
}

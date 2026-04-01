package de.frauas.notenmodulservice.client;

import de.frauas.notenmodulservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * REST-Client für die Kommunikation mit dem User-Service.
 * 
 * <p>Verwendet Service-to-Service-Authentifizierung via API-Key.</p>
 */
@Component
public class UserServiceClient {

    private static final String SERVICE_API_KEY_HEADER = "X-Service-API-Key";
    private static final String SERVICE_NAME_HEADER = "X-Service-Name";

    private final RestClient userServiceRestClient;
    private final String serviceName;
    private final String serviceApiKey;

    public UserServiceClient(
            RestClient userServiceRestClient,
            @Value("${apikey.name}") String serviceName,
            @Value("${apikey.key}") String serviceApiKey) {
        this.userServiceRestClient = userServiceRestClient;
        this.serviceName = serviceName;
        this.serviceApiKey = serviceApiKey;
    }

    /**
     * Lädt Benutzerprofil anhand der Matrikelnummer vom User-Service.
     * 
     * @param matrikelnummer Matrikelnummer des Benutzers
     * @return Benutzerprofil-Daten
     * @throws NotFoundException wenn der Benutzer nicht gefunden wurde
     */
    public UserProfileResponse getUserByMatrikelnummer(String matrikelnummer) {
        try {
            return userServiceRestClient.get()
                    .uri("/api/v1/users/internal/by-matrikelnummer/{matrikelnummer}", matrikelnummer)
                    .header(SERVICE_API_KEY_HEADER, serviceApiKey)
                    .header(SERVICE_NAME_HEADER, serviceName)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(UserProfileResponse.class);
        } catch (RestClientResponseException ex) {
            HttpStatusCode status = ex.getStatusCode();
            if (status.value() == 404) {
                throw new NotFoundException("Benutzer mit Matrikelnummer " + matrikelnummer + " nicht gefunden");
            }
            throw new RuntimeException("User-Service nicht erreichbar (Status: " + status.value() + ")");
        }
    }
}

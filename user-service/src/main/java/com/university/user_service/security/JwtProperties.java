package com.university.user_service.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Konfigurationsklasse für JWT-Eigenschaften.
 * 
 * <p>Lädt Werte aus application.yaml unter dem Prefix "jwt":
 * <ul>
 *   <li>jwt.secret - Signatur-Secret für Token-Erstellung/-Validierung</li>
 *   <li>jwt.expiration-ms - Token-Ablaufzeit in Millisekunden</li>
 * </ul>
 * 
 * <p><b>Sicherheitshinweis:</b> In Produktion MUSS das Secret über die 
 * Umgebungsvariable JWT_SECRET gesetzt werden. Der Default-Wert ist nur 
 * für Entwicklung gedacht!
 * 
 * @see JwtService
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    /**
     * JWT-Signatur-Secret. Muss mindestens 64 Zeichen lang sein für HS256.
     * In Produktion: export JWT_SECRET="<sicheres-secret>"
     */
    private String secret;
    
    /**
     * Token-Ablaufzeit in Millisekunden. Default: 3600000 (1 Stunde)
     */
    private long expirationMs;
}

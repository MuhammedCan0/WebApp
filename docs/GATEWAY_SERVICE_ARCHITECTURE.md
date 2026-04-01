# Gateway-Service - Architektur und Schnittstellendefinition

## 1. Übersicht

### 1.1 Zweck des Services
Der Gateway-Service ist der zentrale Einstiegspunkt für alle Client-Anfragen und verantwortlich für:
- **Request-Routing**: Weiterleitung von Anfragen an Backend-Services
- **JWT-Validierung**: Zentrale Authentifizierung aller geschützten Endpunkte
- **Claims-Extraktion**: JWT-Claims werden als HTTP-Header an Backend-Services weitergeleitet
- **Rate-Limiting**: Schutz vor Überlastung durch Token-Bucket-Algorithmus
- **Request-Logging**: Protokollierung aller ein- und ausgehenden Anfragen

### 1.2 Architektur-Position
```
┌─────────────────────────────────────────────────────────────────────┐
│                        Client (Browser/App)                         │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   Gateway-Service (Port 8080)                        │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ 1. GatewayLoggingFilter     - Request/Response Logging        │  │
│  │ 2. JwtClaimsExtractionFilter - JWT Validierung & Extraktion   │  │
│  │ 3. GatewayRoutesConfig      - Routing zu Backend-Services     │  │
│  └───────────────────────────────────────────────────────────────┘  │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │
        ▼                        ▼                        ▼
┌───────────────┐    ┌───────────────────┐    ┌─────────────────┐
│ User-Service  │    │ Auth-Service      │    │ Tenant-Service  │
│  (Port 8081)  │    │  (Port 8085)      │    │  (Port 8084)    │
└───────────────┘    └───────────────────┘    └─────────────────┘
        │
        ▼
┌───────────────────────────────────────────────────────────────────┐
│                                                                   │
│    ┌─────────────────────────┐    ┌─────────────────────────┐     │
│    │ Noten-Modulverwaltung   │    │ Notenberechnung-Service │     │
│    │ (Port 8082, GraphQL)    │    │ (Port 8083, REST)       │     │
│    └─────────────────────────┘    └─────────────────────────┘     │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

### 1.3 Technologie-Stack
| Komponente | Technologie |
|------------|-------------|
| Framework | Spring Boot 3.x |
| Gateway | Spring Cloud Gateway MVC |
| Sprache | Java 17 |
| Token-Validierung | JJWT (JSON Web Token) |
| Build-Tool | Maven |
| Container | Docker |
| Port | 8080 |

---

## 2. Routing-Konfiguration

### 2.1 Routen-Übersicht

| Priorität | Gateway-Pfad | Ziel-Service | Ziel-Pfad | Beschreibung |
|-----------|--------------|--------------|-----------|--------------|
| 1 | `/graphql`, `/graphiql` | Noten-Modulverwaltung (8082) | `/graphql`, `/graphiql` | GraphQL API |
| 2 | `/api/grades/**` | Notenberechnung (8083) | `/api/grades/**` | Studentennoten |
| 2 | `/api/{poId}/grades/**` | Notenberechnung (8083) | `/api/{poId}/grades/**` | PO-spezifische Noten |
| 2 | `/api/{poId}/modules/**` | Notenberechnung (8083) | `/api/{poId}/modules/**` | PO-spezifische Module |
| 3 | `/api/tenants/**` | Tenant-Service (8084) | `/api/v1/tenants/**` | Mandantenverwaltung |
| 4 | `/api/auth/**` | Auth-Service (8085) | `/api/v1/auth/**` | Authentifizierung |
| 5 | `/api/**` | User-Service (8081) | `/api/v1/**` | Benutzerverwaltung |

### 2.2 URL-Rewriting
Das Gateway führt automatisches URL-Rewriting durch:
- `/api/tenants/...` → `/api/v1/tenants/...` (Tenant-Service)
- `/api/auth/...` → `/api/v1/auth/...` (Auth-Service)
- `/api/...` → `/api/v1/...` (User-Service)

### 2.3 Umgebungsvariablen für Service-URLs
```
USER_SERVICE_URL=http://localhost:8081
NOTEN_MODUL_SERVICE_URL=http://localhost:8082
NOTENBERECHNUNG_SERVICE_URL=http://localhost:8083
TENANT_SERVICE_URL=http://localhost:8084
AUTH_SERVICE_URL=http://localhost:8085
```

---

## 3. JWT-Validierung und Claims-Extraktion

### 3.1 Öffentliche Endpunkte (ohne JWT)
Folgende Pfade erfordern **keine** Authentifizierung:
- `/api/auth/*` - Login, Register
- `/actuator` - Health-Check und Monitoring
- `/graphiql` - GraphQL-Playground (nur Entwicklung)

### 3.2 Geschützte Endpunkte
Alle anderen Endpunkte erfordern einen gültigen JWT-Token im `Authorization`-Header:
```
Authorization: Bearer <JWT-Token>
```

### 3.3 Claims-zu-Header-Mapping
Nach erfolgreicher JWT-Validierung werden die Claims als HTTP-Header an Backend-Services weitergeleitet:

| JWT Claim | HTTP Header | Beschreibung |
|-----------|-------------|--------------|
| `sub` (Subject) | `X-User-Id` | Benutzer-ID |
| `email` | `X-User-Email` | E-Mail-Adresse |
| `role` | `X-User-Roles` | Rollen (komma-separiert) |
| `tenant_ids` | `X-User-Tenant-Ids` | Mandanten-IDs (komma-separiert) |
| `studiengaenge` | `X-User-Studiengaenge` | Studiengänge (komma-separiert) |
| `matrikelnummer` | `X-User-Matrikelnummer` | Matrikelnummer |

### 3.4 Vorteile der zentralen Validierung
- Backend-Services müssen JWT nicht erneut validieren
- Zentrale Authentifizierungslogik im Gateway
- Backend-Services können Claims aus Headers lesen (performanter als JWT-Parsing)
- Einheitliche Fehlerbehandlung bei ungültigen Tokens

---

## 4. Fehlerbehandlung

### 4.1 Authentifizierungsfehler
Bei fehlenden oder ungültigen JWT-Tokens gibt das Gateway eine JSON-Fehlermeldung zurück:

```json
{
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header",
  "status": 401
}
```

### 4.2 Fehlercodes
| Code | Fehler | Beschreibung |
|------|--------|--------------|
| 401 | Missing Authorization | Kein Authorization-Header vorhanden |
| 401 | Invalid JWT token | Token ist ungültig oder manipuliert |
| 401 | JWT token has expired | Token ist abgelaufen |

---

## 5. Rate-Limiting

### 5.1 Konfiguration
Das Gateway implementiert Rate-Limiting über einen Token-Bucket-Algorithmus:

```yaml
gateway:
  rate-limit:
    enabled: true
    replenishRate: 10     # Tokens pro Minute
    burstCapacity: 10     # Maximale Tokens (Burst)
    windowSeconds: 60     # Zeitfenster in Sekunden
```

### 5.2 Verhalten
- Jeder Client erhält maximal 10 Requests pro Minute
- Burst-Anfragen bis zu 10 gleichzeitige Requests erlaubt
- Bei Überschreitung: HTTP 429 (Too Many Requests)

---

## 6. Logging

### 6.1 Request-Logging
Der `GatewayLoggingFilter` protokolliert alle Anfragen:

**Eingehende Anfrage:**
```
Incoming request: method=GET, path=/api/users, clientIp=192.168.1.100
```

**Abgeschlossene Anfrage:**
```
Completed request: method=GET, path=/api/users, status=200, clientIp=192.168.1.100, durationMs=45
```

### 6.2 Log-Levels
```yaml
logging:
  level:
    root: INFO
    "[org.springframework.cloud.gateway]": DEBUG
    "[com.university.gateway_service]": DEBUG
```

---

## 7. Konfiguration

### 7.1 application.yaml
```yaml
server:
  port: 8080

spring:
  application:
    name: gateway-service

jwt:
  secret: ${JWT_SECRET:meinSuperGeheimerSchluessel...}

gateway:
  rate-limit:
    enabled: true
    replenishRate: 10
    burstCapacity: 10
    windowSeconds: 60
```

### 7.2 Umgebungsvariablen
| Variable | Beschreibung | Default |
|----------|--------------|---------|
| `JWT_SECRET` | Secret für JWT-Signatur | (dev-secret) |
| `USER_SERVICE_URL` | URL des User-Service | http://localhost:8081 |
| `NOTEN_MODUL_SERVICE_URL` | URL des Noten-Modul-Service | http://localhost:8082 |
| `NOTENBERECHNUNG_SERVICE_URL` | URL des Notenberechnung-Service | http://localhost:8083 |
| `TENANT_SERVICE_URL` | URL des Tenant-Service | http://localhost:8084 |
| `AUTH_SERVICE_URL` | URL des Auth-Service | http://localhost:8085 |

---

## 8. Projektstruktur

```
gateway-service/
├── src/main/java/com/university/gateway_service/
│   ├── config/
│   │   ├── GatewayLoggingFilter.java      # Request/Response Logging
│   │   ├── GatewayRoutesConfig.java       # Routing-Konfiguration
│   │   └── JwtClaimsExtractionFilter.java # JWT-Validierung & Claims
│   └── GatewayServiceApplication.java     # Main-Klasse
├── src/main/resources/
│   └── application.yaml                   # Konfiguration
├── Dockerfile
└── pom.xml
```

---

## 9. API-Beispiele

### 9.1 Login (nicht authentifiziert)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "secret"}'
```

### 9.2 Geschützte Anfrage
```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <JWT-TOKEN>"
```

### 9.3 GraphQL-Anfrage
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Authorization: Bearer <JWT-TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"query": "{ alleNoten { id matrikelnummer note status } }"}'
```

---

## 10. Lokaler Start

### Voraussetzungen
- Java 17
- Maven Wrapper (liegt im Projekt)

### Starten
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# macOS/Linux
./mvnw spring-boot:run
```

Service läuft anschließend unter: http://localhost:8080

---

## 11. Hinweis zur Erstellung

Bei der Erstellung dieser Dokumentation wurde KI-Unterstützung für den Feinschliff und die Vereinheitlichung der Formatierung verwendet. Die fachlichen Inhalte, Architekturentscheidungen und technische Spezifikation wurden eigenständig erarbeitet.

# Zwischenstand Meilenstein 3 - Gateway & Integration

**Stand:** 25. Januar 2026  
**Projektname:** Hochschul-Verwaltungssoftware (webapp-404-brain-not-found)  
**Team:** 404 Brain Not Found

---

## Zielbeschreibung

Implementierung eines zentralen API-Gateways als einheitlicher Einstiegspunkt für alle Microservices. Das Gateway übernimmt die JWT-Validierung, das Request-Routing zu den Backend-Services sowie Logging und Rate-Limiting. Dadurch wird eine lose Kopplung der Services gewährleistet und die Sicherheit zentral gesteuert.

---

## Erwartetes Ergebnis

- **Funktionsfähiges API-Gateway** auf Port 8080, das alle Anfragen an die entsprechenden Services routet
- **Zentrale JWT-Authentifizierung** mit Claims-Extraktion und Weiterleitung als HTTP-Header
- **Einheitliche Routing-Konfiguration** für alle Services (Auth, GraphQL, Notenberechnung)
- **Request-Logging** zur Nachverfolgung und Debugging
- **Rate-Limiting** zum Schutz vor Überlastung
- **Dokumentation** des Gateways und der Routing-Regeln

---

## Übersicht der Tasks

| Task | Beschreibung | Status | Fortschritt |
|------|--------------|--------|-------------|
| **3.1** | Tech-Stack initialisieren | ✅ Abgeschlossen | 100% |
| **3.2** | API-Gateway implementieren | ✅ Abgeschlossen | 100% |
| **3.3** | Zwischenstand dokumentieren | ✅ Abgeschlossen | 100% |

---

## 3.1 - Tech-Stack initialisieren ✅

### Verwendete Technologien

| Komponente | Technologie | Version |
|------------|-------------|---------|
| **Framework** | Spring Boot | 3.5.9 |
| **Gateway** | Spring Cloud Gateway MVC | 2025.0.1 |
| **JWT** | JJWT (jsonwebtoken) | 0.12.x |
| **Java** | OpenJDK | 17 |
| **Build** | Maven | 3.x |

### Projektstruktur Gateway-Service

```
gateway-service/
├── src/main/java/com/university/gateway_service/
│   ├── GatewayServiceApplication.java
│   └── config/
│       ├── GatewayRoutesConfig.java      # Routing-Konfiguration
│       ├── JwtClaimsExtractionFilter.java # JWT-Validierung
│       └── GatewayLoggingFilter.java      # Request-Logging
├── src/main/resources/
│   └── application.yaml                   # Konfiguration
└── pom.xml
```

---

## 3.2 - API-Gateway implementieren ✅

### Implementierte Features

#### 1. Zentrales Routing

Das Gateway (Port 8080) routet Anfragen an die Backend-Services:

| Gateway-Pfad | Ziel-Service | Port |
|--------------|--------------|------|
| `/api/auth/**` | User-Tenant-Auth-Service | 8081 |
| `/api/users/**` | User-Tenant-Auth-Service | 8081 |
| `/api/tenants/**` | User-Tenant-Auth-Service | 8081 |
| `/graphql` | Noten-Modulverwaltung-Service | 8082 |
| `/graphiql` | Noten-Modulverwaltung-Service | 8082 |
| `/api/grades/**` | Notenberechnung-Service | 8083 |
| `/api/{poId}/grades/**` | Notenberechnung-Service | 8083 |
| `/api/{poId}/modules/**` | Notenberechnung-Service | 8083 |

**Routing-Priorität (via @Order):**
1. GraphQL-Routen (Noten-Modulverwaltung)
2. Notenberechnung-Routen
3. Auth-Service (Catch-All für `/api/**`)

#### 2. JWT-Validierung & Claims-Extraktion

**JwtClaimsExtractionFilter** - Zentrale Authentifizierung:

- ✅ JWT aus `Authorization: Bearer <token>` extrahieren
- ✅ Token-Signatur validieren
- ✅ Token-Ablauf prüfen (ExpiredJwtException)
- ✅ Claims (userId, email, roles) extrahieren
- ✅ Claims als HTTP-Header weiterleiten:
  - `X-User-Id`
  - `X-User-Email`
  - `X-User-Roles`

**Öffentliche Endpunkte (ohne JWT):**
- `/api/auth/*` - Login, Register
- `/actuator/*` - Health-Checks
- `/graphiql` - GraphQL-Playground (Entwicklung)

#### 3. Request-Logging

**GatewayLoggingFilter** - Debug-Logging:
- HTTP-Methode
- Request-URI
- Response-Status
- Verarbeitungszeit

#### 4. Konfiguration

**application.yaml:**
```yaml
server:
  port: 8080

jwt:
  secret: ${JWT_SECRET:meinSuperGeheimerSchluessel...}

gateway:
  rate-limit:
    enabled: true
    replenishRate: 10      # Tokens pro Minute
    burstCapacity: 10      # Max Burst
    windowSeconds: 60
```

---

## Architektur-Übersicht

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (Browser/App)                      │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Gateway-Service (Port 8080)                   │
│          JWT-Validierung • Routing • Rate-Limiting              │
│                                                                 │
│  ┌─────────────────┐  ┌──────────────────┐  ┌────────────────┐ │
│  │ JwtClaimsFilter │  │ GatewayRoutes    │  │ LoggingFilter  │ │
│  └─────────────────┘  └──────────────────┘  └────────────────┘ │
└────────────────────────────┬────────────────────────────────────┘
                             │
         ┌───────────────────┼───────────────────┐
         ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ User-Tenant-Auth│ │ Noten-Modul-    │ │ Notenberechnung │
│ Service (8081)  │ │ Service (8082)  │ │ Service (8083)  │
│   REST-API      │ │   GraphQL-API   │ │   REST-API      │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

---

## 3.3 - Zwischenstand dokumentieren ✅

**Erstellte Dokumentation:**
- README.md (aktualisiert mit Gateway-Informationen)
- Architekturdiagramme
- API-Routing-Dokumentation

---

## Kommunikationsfluss

### Beispiel: Authentifizierter Request

```
1. Client → Gateway (8080)
   POST /api/users
   Authorization: Bearer <JWT>

2. Gateway: JwtClaimsExtractionFilter
   - JWT validieren ✓
   - Claims extrahieren: userId=1, roles=ADMIN

3. Gateway → Auth-Service (8081)
   POST /api/v1/users
   X-User-Id: 1
   X-User-Email: admin@hochschule.de
   X-User-Roles: ADMIN

4. Auth-Service → Gateway → Client
   Response: 201 Created
```

### Beispiel: GraphQL-Request

```
1. Client → Gateway (8080)
   POST /graphql
   Authorization: Bearer <JWT>
   Body: {"query": "{ alleModule { modulId } }"}

2. Gateway: JWT validieren, Claims extrahieren

3. Gateway → Noten-Modulverwaltung (8082)
   POST /graphql
   X-User-Id: 1
   X-User-Roles: STUDENT

4. GraphQL-Service → Gateway → Client
   Response: {"data": {"alleModule": [...]}}
```

---

## Ergebnisse & Erfolge

| Kriterium | Status | Anmerkung |
|-----------|--------|-----------|
| Zentrales Routing | ✅ | Alle Services erreichbar über Port 8080 |
| JWT-Validierung | ✅ | Zentral im Gateway |
| Claims-Weiterleitung | ✅ | X-User-* Headers |
| Rate-Limiting | ✅ | Konfigurierbar |
| Logging | ✅ | Request/Response Logging |
| Path-Rewriting | ✅ | /api/* → /api/v1/* |

---

## Lessons Learned

1. **Spring Cloud Gateway MVC vs. Reactive:**
   - Verwendung der MVC-Variante (nicht WebFlux) für bessere Kompatibilität

2. **Route-Reihenfolge:**
   - `@Order` Annotation wichtig für korrekte Route-Matching-Reihenfolge

3. **JWT-Secret:**
   - Muss zwischen Gateway und Auth-Service übereinstimmen
   - Umgebungsvariable `JWT_SECRET` für Produktion

---

## Nächste Schritte

→ **Meilenstein 4:** Qualitätssicherung
- Postman-Collection erstellen
- Systemtests durchführen
- Bugfixing

---

## Verantwortlichkeiten

| Task | Zuständig | Vertretung |
|------|-----------|------------|
| 3.1 | Mustafa | Jin |
| 3.2 | Alle | Alle |
| 3.3 | Muhammed Can | Mustafa |

---

**Vorheriger Meilenstein:** 2 - Service-Implementierung  
**Nächster Meilenstein:** 4 - Qualitätssicherung

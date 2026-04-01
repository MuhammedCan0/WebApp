# Zwischenstand Meilenstein 2 - Microservice-Architektur

**Stand:** 13. Februar 2026  
**Projektname:** Hochschul-Verwaltungssoftware (webapp-404-brain-not-found)  
**Team:** 404 Brain Not Found

---

## Zielbeschreibung

Das Ziel dieses Dokuments ist die Besprechung und Dokumentation der Fortschritte der Arbeitspakete 2.1, 2.2, 2.3, 2.4, 2.5 und 2.6 im Rahmen des Meilensteins 2 (Microservice-Architektur realisiert). Es werden der aktuelle Entwicklungsstand, bereits implementierte Funktionen sowie noch offene Aufgaben dargestellt.

---

## Erwartetes Ergebnis

- **Anpassung des aktuellen Projektplans** ermöglichen
- **Aktualisierte Aufgabenliste** bereitstellen
- **Bestehende Probleme** identifizieren und klären
- **Fortschritt der einzelnen Arbeitspakete** transparent dokumentieren

---

## Übersicht der Tasks

| Task | Beschreibung | Status | Fortschritt |
|------|--------------|--------|-------------|
| **2.1** | Tech-Stack initialisieren | ✅ Abgeschlossen | 100% |
| **2.2** | User-/Rollen-/Auth-Service erstellen | ✅ Abgeschlossen | 100% |
| **2.3** | Microservice Noten-/Modul-Verwaltung erstellen | ✅ Abgeschlossen | 100% |
| **2.4** | Mikroservice Notenberechnung erstellen | ✅ Abgeschlossen | 100% |
| **2.5** | Kommunikationsschnittstellen erstellen | ✅ Abgeschlossen | 100% |
| **2.6** | Überarbeitung aller relevanten Services auf Tenant-ID Logik | ✅ Abgeschlossen | 100% |
| **2.7** | Zwischenstand dokumentieren | ✅ Abgeschlossen | 100% |

---

## Service-Übersicht

| Service | Port | Beschreibung |
|---------|------|--------------|
| **User-/Rollen-/Auth-Service** | 8081 | REST-Mikroservice für Authentifizierung und Benutzerverwaltung |
| **Noten-/Modul-Verwaltung** | 8082 | GraphQL-Mikroservice für Noten- und Modulverwaltung |
| **Notenberechnung** | 8083 | REST-Mikroservice für Gesamtnotenberechnung nach Prüfungsordnung |

---

## 2.1 - Tech-Stack initialisieren ✅

**Zuständig:** Alle  
**Vertretung:** Mustafa

### Verwendete Technologien

| Komponente | Technologie | Version |
|------------|-------------|---------|
| **Framework** | Spring Boot | 3.2.1 |
| **Java** | OpenJDK | 17 |
| **Build** | Maven | 3.x |
| **Datenbank** | MySQL | - |
| **Security** | Spring Security + JWT | - |

### Projektstruktur

Die Spring Boot Projekte für alle drei Microservices wurden erfolgreich initialisiert und konfiguriert:
- Eigenes Spring Boot Projekt pro Service mit Spring Initializr
- Maven-Standardkonvention mit separaten Ordnern für Controller, Services, Repositories und Entities
- Eigene `application.yaml` pro Service mit Port-Konfigurationen und Datenbank-Verbindungsparametern

### Dependencies

- Spring Web
- Spring Data JPA
- MySQL Connector
- Spring Security
- Lombok

---

## 2.2 - User-/Rollen-/Auth-Service erstellen ✅

**Zuständig:** Mustafa  
**Vertretung:** Betül

### Datenmodell und Persistenz

| Entity | Beschreibung |
|--------|--------------|
| **User** | ID, E-Mail, Passwort-Hash (BCrypt), Vorname, Nachname, Matrikelnummer, Relationen zu Role und Studiengang |
| **Role** | STUDENT, DOZENT, PRUEFUNGSAMT, ADMIN |
| **Studiengang** | Informatik, Wirtschaftsinformatik, Medieninformatik |

### REST-Endpunkte

| Endpunkt | Methode | Beschreibung | Zugriff |
|----------|---------|--------------|---------|
| `/api/auth/login` | POST | Authentifizierung, JWT-Token-Rückgabe | Öffentlich |
| `/api/auth/register` | POST | Neue Benutzer registrieren | ADMIN, PRUEFUNGSAMT |
| `/api/users` | GET | Alle Benutzer abrufen | ADMIN, PRUEFUNGSAMT |
| `/api/users/{id}` | GET | Spezifischen Benutzer abrufen | Je nach Rolle |
| `/api/users/{id}` | PUT | Benutzerdaten aktualisieren | Je nach Rolle |
| `/api/users/{id}` | DELETE | Benutzer löschen | ADMIN, PRUEFUNGSAMT |

### JWT-Token-Generierung

- Bibliothek: java-jwt (auth0)
- Claims: userId (Subject), email, role, studiengang
- Standard-Claims: iat (issued at), exp (expiration)
- Gültigkeit: 24 Stunden
- Signatur: 256-Bit Secret-Key

### Sicherheitskonfiguration

- Login-Endpunkt ohne Authentifizierung erreichbar
- Alle anderen Endpunkte erfordern gültige JWT-Authentifizierung
- JwtAuthenticationFilter für Token-Validierung
- GlobalExceptionHandler mit @ControllerAdvice für einheitliche Fehlerbehandlung

---

## 2.3 - Microservice Noten-/Modul-Verwaltung erstellen ✅

**Zuständig:** Jin, Betül  
**Vertretung:** Betül

### Datenmodell

| Entity | Beschreibung |
|--------|--------------|
| **Modul** | Modulcode, Modulname, Credits (ECTS), Semester, Studiengang, Dozent-ID |
| **Note** | Student-Modul-Verbindung, Punktzahl, Note, Status (ERFASST/VALIDIERT/FREIGEGEBEN), Prüfungsdatum |
| **Pruefungsleistung** | Teilleistungen pro Modul (Klausur, Hausarbeit, mündliche Prüfung) |

### GraphQL-Schema

**Types:**
- `Modul` - Studienmodul mit allen relevanten Informationen
- `Note` - Prüfungsleistung eines Studierenden
- `Query` - Abfrage-Operationen
- `Mutation` - Änderungs-Operationen

**Queries:**

| Query | Beschreibung | Zugriff |
|-------|--------------|---------|
| `alleModule()` | Alle Module abrufen | Alle Rollen |
| `modulById(id)` | Spezifisches Modul abrufen | Alle Rollen |
| `alleNotenVonStudent(studentId)` | Alle Noten eines Studierenden | Student selbst, Dozenten, PRUEFUNGSAMT, ADMIN |
| `meineNoten()` | Eigene Noten | Angemeldeter Student |

**Mutations:**

| Mutation | Beschreibung | Zugriff |
|----------|--------------|---------|
| `modulErstellen()` | Neues Modul erstellen | ADMIN, PRUEFUNGSAMT |
| `noteErfassen()` | Note erfassen | DOZENT (eigenes Modul) |
| `noteBearbeiten()` | Note bearbeiten | DOZENT (nicht validiert) |
| `noteValidieren()` | Note validieren | PRUEFUNGSAMT |
| `noteFreigeben()` | Note freigeben | PRUEFUNGSAMT |

### Autorisierung

- Auswertung der HTTP-Header: `X-User-Id`, `X-User-Email`, `X-User-Roles`
- AuthorizationService für Berechtigungsprüfung
- GraphiQL-Interface unter `/graphiql` für Entwicklung

---

## 2.4 - Mikroservice Notenberechnung erstellen ✅

**Zuständig:** Can  
**Vertretung:** Mustafa

### REST-Endpunkte

| Endpunkt | Beschreibung |
|----------|--------------|
| `GET /api/berechnung/gesamtnote/{studentId}` | Gewichtete Gesamtnote berechnen |
| `GET /api/berechnung/studienverlauf/{studentId}` | Detaillierte Studienverlauf-Übersicht |
| `GET /api/berechnung/fehlende-credits/{studentId}` | Fehlende Credits bis Abschluss |

### Berechnungslogik

**Formel:** `Gesamtnote = Summe(Note × Credits) / Summe(Credits)`

**Regeln gemäß Prüfungsordnung:**
- Nur Module mit Status „FREIGEGEBEN" werden berücksichtigt
- Module mit Note 5,0 (nicht bestanden) werden ausgeschlossen
- Bei Wiederholungsprüfungen wird nur das beste Ergebnis berücksichtigt
- Gesamtnote auf zwei Nachkommastellen kaufmännisch gerundet

### Response-Struktur

```json
{
  "studentId": 123,
  "gesamtnote": 2.34,
  "erreichteCredits": 120,
  "erforderlicheCredits": 180,
  "anzahlModule": 20,
  "berechnungsDatum": "2026-01-05T10:30:00"
}
```

### Fehlerbehandlung

| Szenario | HTTP-Status |
|----------|-------------|
| Keine freigegebenen Noten | 404 Not Found |
| Kommunikationsprobleme (nach 3 Retries) | 503 Service Unavailable |
| Ungültige studentId | 400 Bad Request |

---

## 2.5 - Kommunikationsschnittstellen erstellen ✅

**Zuständig:** Jin, Betül  
**Vertretung:** Betül

### REST-Client

- **NotenModulClient** für HTTP-Kommunikation
- Spring's WebClient für nicht-blockierende, reaktive HTTP-Anfragen
- Service-URL in `application.yaml` konfiguriert

### GraphQL-Client-Integration

GraphQL-Queries als JSON-Payloads an `/graphql`-Endpunkt:

```graphql
query {
  alleNotenVonStudent(studentId: 123) {
    id
    modul { modulcode, name, credits }
    punktzahl
    note
    status
    datum
  }
}
```

### Authentifizierungs-Weiterleitung

- JWT-Token aus Request-Context extrahieren
- Authorization-Header bei allen ausgehenden Anfragen mitschicken
- Service-zu-Service-Kommunikation im Benutzerkontext

### Timeout und Retry-Konfiguration

| Parameter | Wert |
|-----------|------|
| Connection Timeout | 5 Sekunden |
| Read Timeout | 10 Sekunden |
| Write Timeout | 10 Sekunden |
| Max Retries | 3 |
| Backoff | 1s → 2s → 4s (Exponential) |

### Circuit Breaker (Resilience4j)

- Öffnet bei >50% Fehlerrate innerhalb 60 Sekunden
- Geöffnet: Anfragen sofort mit Fehler beantwortet
- Nach 30 Sekunden: Half-Open-Zustand
- Test-Anfragen für Recovery

### Daten-Caching

- Spring's `@Cacheable`-Annotation
- Notendaten für 5 Minuten gecached
- Cache-Invalidierung bei Änderungen

### Monitoring und Logging

- Detailliertes Logging aller Service-Aufrufe
- Request-URL, Parameter, Response-Status, Ausführungsdauer
- Metriken für Prometheus/Grafana vorbereitet

---

## 2.6 - Überarbeitung aller relevanten Services auf Tenant-ID Logik ✅

**Zuständig:** Alle  
**Vertretung:** Alle

### Umgesetzte Maßnahmen

| Maßnahme | Beschreibung |
|----------|-------------|
| **Service-Klassen überarbeitet** | Alle relevanten Service-Methoden berücksichtigen nun die Tenant-ID |
| **Datenzugriffe angepasst** | Alle Operationen auf Multi-Tenant-Betrieb (mandantenfähig) umgestellt |
| **Kontrollmechanismen** | Service-Zugriffe sind tenant-isoliert |
| **Modul-/Noten-Komponenten** | Aktualisierung auf Mandantenfähigkeit und rollenbasierten Zugriff via JWT |
| **Tests & Konfigurationen** | Anpassung und Aktualisierung auf die neuen Anforderungen |

### Prüfaufgaben

- [x] Sicherstellen, dass in allen Services die Tenant-ID korrekt verarbeitet wird
- [x] Code-Stellen reviewen, die Daten filtern, speichern oder laden (Repository & DTO Anpassungen)
- [x] Testen der Mandantenfähigkeit, inkl. JWT- und rollenbasiertem Zugriff

### Betroffene Komponenten

| Service | Anpassungen |
|---------|-------------|
| **User-Tenant-Auth-Service** | Tenant-ID in User-Entity, Tenant-Filterung bei Abfragen |
| **Noten-Modulverwaltung** | GraphQL-Resolver mit Tenant-Validierung, Repository-Filterung |
| **Notenberechnung** | Tenant-ID bei Berechnungsanfragen berücksichtigen |

---

## 2.7 - Zwischenstand dokumentieren ✅

**Zuständig:** Jin  
**Vertretung:** Betül

Dieses Dokument.

---

## Architektur-Übersicht

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (Browser/App)                      │
└────────────────────────────┬────────────────────────────────────┘
                             │
         ┌───────────────────┼───────────────────┐
         ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ User-Tenant-Auth│ │ Noten-Modul-    │ │ Notenberechnung │
│ Service (8081)  │ │ Service (8082)  │ │ Service (8083)  │
│   REST-API      │ │   GraphQL-API   │ │   REST-API      │
│   JWT-Ausgabe   │ │   Datenverwalt. │ │   PO-Berechnung │
└─────────────────┘ └────────┬────────┘ └────────┬────────┘
                             │                   │
                             └───────────────────┘
                           Service-zu-Service
                           Kommunikation
```

---

## Herausforderungen und Lösungen

### 1. GraphQL-Autorisierung

**Problem:** Standard-GraphQL bietet keine integrierte Autorisierung auf Resolver-Ebene.

**Lösung:** Custom AuthorizationService, der HTTP-Header auswertet. Eigene `@Authorized`-Annotationen für Resolver-Methoden.

### 2. Service-zu-Service-Authentifizierung

**Problem:** Benutzerkontext muss bei Service-zu-Service-Kommunikation erhalten bleiben.

**Lösung:** JWT-Token aus Request-Context extrahieren und bei ausgehenden Requests im Authorization-Header mitschicken (custom WebClient-Filter).

### 3. Datenbankschema-Synchronisation

**Problem:** Konflikte bei Schema-Initialisierung im Team.

**Lösung:** Flyway als Database Migration Tool. Versionierte SQL-Migrationsskripte für konsistenten Datenbankstand.

### 4. Performance bei GraphQL-Queries

**Problem:** N+1-Query-Probleme bei verschachtelten Beziehungen.

**Lösung:** DataLoader-Pattern für Batch-Abfragen. `@EntityGraph`-Annotationen für Eager Loading.

---

## Vorteile der Architektur

| Vorteil | Beschreibung |
|---------|--------------|
| **Skalierbarkeit** | Jeder Service unabhängig skalierbar (z.B. Notenberechnung in Prüfungsphasen) |
| **Wartbarkeit** | Klare Trennung der Zuständigkeiten, unabhängige Änderungen |
| **Technologievielfalt** | REST und GraphQL je nach Anforderung |
| **Fehlertoleranz** | Circuit Breaker und Retry-Mechanismen |

---

## Offene Punkte

Zum aktuellen Zeitpunkt gibt es **keine kritischen offenen Punkte**. Alle Arbeitspakete wurden erfolgreich abgeschlossen und getestet.

### Optionale Verbesserungen (zukünftige Iterationen)

- Service Discovery (Eureka/Consul) für dynamisches Routing
- Distributed Tracing mit Sleuth und Zipkin
- GraphQL-Subscriptions für Echtzeit-Benachrichtigungen
- Test-Coverage auf mindestens 80% erhöhen

---

## Nächste Schritte

→ **Meilenstein 3:** Gateway & Integration

- API-Gateway als zentraler Einstiegspunkt (Port 8080)
- Zentrale JWT-Validierung
- Routing zu Backend-Services
- Rate Limiting und Throttling
- Zentrales Logging und Monitoring

---

## Verantwortlichkeiten

| Task | Zuständig | Vertretung |
|------|-----------|------------|
| 2.1 | Alle | Mustafa |
| 2.2 | Mustafa | Betül |
| 2.3 | Jin, Betül | Mustafa |
| 2.4 | Can | Mustafa |
| 2.5 | Jin, Betül | Betül |
| 2.6 | Alle | Alle |
| 2.7 | Jin | Betül |

---

**Vorheriger Meilenstein:** 1 - Planung & Anforderungen  
**Nächster Meilenstein:** 3 - Gateway & Integration

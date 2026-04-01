# Noten-Modulverwaltungsservice - Architektur und Schnittstellendefinition

## 1. Übersicht

### 1.1 Zweck des Services
Der Noten-Modulverwaltungsservice ist verantwortlich für:
- **Notenverwaltung**: CRUD-Operationen für Studentennoten
- **Modulverwaltung**: Verwaltung von Studienmodulen pro Tenant
- **Notenprozess-Workflow**: Status-Management (ADD → TO_VALIDATE → PUBLISHED)
- **Rollenbasierte Zugriffskontrolle**: Unterschiedliche Berechtigungen für Studenten, Lehrende, Prüfungsamt
- **Tenant-Isolation**: Mandantenfähigkeit für verschiedene Hochschulen/Fakultäten
- **GraphQL API**: Flexible Abfragen und Mutations über GraphQL

### 1.2 Architektur-Position
```
┌──────────────────────────────────────────────────┐
│         Gateway-Service (Port 8080)              │
│    JWT Validation & X-User-* Headers             │
└────────────────┬─────────────────────────────────┘
                 │
         /graphql, /graphiql
                 │
┌────────────────▼──────────────────────────────────┐
│  Noten-Modulverwaltungsservice (Port 8082)        │
├───────────────────────────────────────────────────┤
│ ▼ GraphQL Queries:                                │
│   - alleNoten, notenVonStudent, noteById          │
│   - notenNachStatus, notenVonLehrender            │
│   - notenPruefungsamtZuValidieren                 │
│   - alleModule, modulById, moduleNachStudiengang  │
│   - studentInfo                                   │
│                                                   │
│ ▼ GraphQL Mutations:                              │
│   - noteAnlegen, noteSetzen, notenValidieren      │
│   - noteLoeschen, modulErstellen, modulAktualisieren │
│   - modulLoeschen                                 │
└────────────────┬──────────────────────────────────┘
                 │
         REST-Call (X-Service-API-Key)
                 │
    ┌────────────▼───────────────┐
    │ User-Service               │
    │ (Port 8081)                │
    │ Abruf von Studentendaten   │
    └────────────────────────────┘
```

### 1.3 Technologie-Stack
| Komponente | Technologie |
|------------|-------------|
| Framework | Spring Boot 3.5.x |
| API | Spring GraphQL |
| Sprache | Java 17 |
| Persistenz | Spring Data JPA / Hibernate |
| Datenbank | H2 (Dev) / PostgreSQL (Prod) |
| Token | JWT (JJWT) |
| Build-Tool | Maven |
| Container | Docker |
| Port | 8082 |

---

## 2. Datenmodell

### 2.1 Entity: Note
Eine Note repräsentiert die Bewertung eines Studierenden in einem Modul.

| Feld | Typ | Beschreibung |
|------|-----|--------------|
| `id` | UUID | Eindeutige ID (Primärschlüssel) |
| `matrikelnummer` | String | Matrikelnummer des Studierenden |
| `tenantId` | UUID | Zugehöriger Mandant |
| `modulId` | Integer | Referenz auf das Modul |
| `modulName` | String | Modulname (denormalisiert) |
| `lehrendenMatrikelnummer` | String | Matrikelnummer des Lehrenden |
| `note` | Double | Notenwert (1.0 - 5.0, null wenn noch nicht gesetzt) |
| `status` | NotenStatus | Status der Note (ADD, TO_VALIDATE, PUBLISHED) |
| `studiengang` | String | Studiengang des Studierenden |
| `semester` | String | Semester der Prüfung |
| `erstelltAm` | LocalDateTime | Erstellungszeitpunkt |
| `aktualisiertAm` | LocalDateTime | Letzte Aktualisierung |

### 2.2 Entity: Modul
Ein Modul ist mandantenspezifisch und durch `modulId` + `tenantId` eindeutig identifiziert.

| Feld | Typ | Beschreibung |
|------|-----|--------------|
| `id` | UUID | Technische UUID (Primärschlüssel) |
| `modulId` | Integer | Fachliche Modul-ID (z.B. 1001) |
| `tenantId` | UUID | Zugehöriger Mandant |
| `modulName` | String | Modulname (z.B. "Mathematik I") |
| `ects` | Integer | ECTS-Punkte |
| `gewichtung` | Double | Gewichtungsfaktor für Durchschnitt |
| `studiengang` | String | Zugehöriger Studiengang |
| `semester` | Integer | Empfohlenes Semester |
| `beschreibung` | String | Modulbeschreibung |

### 2.3 Enum: NotenStatus
Der Lebenszyklus einer Note durchläuft folgende Phasen:

```
┌─────────┐     Lehrender      ┌───────────────┐    Prüfungsamt    ┌───────────┐
│   ADD   │ ─── noteSetzen ──► │  TO_VALIDATE  │ ── validieren ──► │ PUBLISHED │
└─────────┘                    └───────────────┘                   └───────────┘
     │                               │
     ▼ (nur Prüfungsamt)             ▼ (bei Korrektur)
  Note anlegen               zurück zu TO_VALIDATE
```

| Status | Beschreibung | Sichtbar für Studenten |
|--------|--------------|------------------------|
| `ADD` | Note angelegt, noch kein Notenwert | ❌ |
| `TO_VALIDATE` | Notenwert gesetzt, wartet auf Validierung | ❌ |
| `PUBLISHED` | Note validiert und veröffentlicht | ✅ |

---

## 3. Rollenbasierte Zugriffskontrolle

### 3.1 Rollen
| Rolle | Beschreibung |
|-------|--------------|
| `STUDENT` | Lesezugriff auf eigene Noten |
| `LEHRENDER` | Noten setzen für eigene Module |
| `PRUEFUNGSAMT` | Noten erstellen, validieren, Module verwalten |
| `ADMIN` | Voller Zugriff, tenant-übergreifend |

### 3.2 Berechtigungsmatrix

| Operation | STUDENT | LEHRENDER | PRUEFUNGSAMT | ADMIN |
|-----------|:-------:|:---------:|:------------:|:-----:|
| `alleNoten` | ❌ | ❌ | ✅ | ✅ |
| `notenVonStudent` | ✅ (eigene) | ❌ | ✅ | ✅ |
| `noteById` | ✅ | ✅ | ✅ | ✅ |
| `notenNachStatus` | ❌ | ❌ | ✅ | ✅ |
| `notenVonLehrender` | ❌ | ✅ (eigene) | ❌ | ✅ |
| `notenPruefungsamtZuValidieren` | ❌ | ❌ | ✅ | ✅ |
| `alleModule` | ✅ | ✅ | ✅ | ✅ |
| `modulById` | ✅ | ✅ | ✅ | ✅ |
| `moduleNachStudiengang` | ✅ | ✅ | ✅ | ✅ |
| `studentInfo` | ✅ (eigene) | ❌ | ✅ | ✅ |
| `noteAnlegen` | ❌ | ❌ | ✅ | ✅ |
| `noteSetzen` | ❌ | ✅ | ❌ | ✅ |
| `notenValidieren` | ❌ | ❌ | ✅ | ✅ |
| `noteLoeschen` | ❌ | ❌ | ✅ | ✅ |
| `modulErstellen` | ❌ | ❌ | ✅ | ✅ |
| `modulAktualisieren` | ❌ | ❌ | ✅ | ✅ |
| `modulLoeschen` | ❌ | ❌ | ✅ | ✅ |

### 3.3 Tenant-Isolation
- Alle Benutzer (außer ADMIN) dürfen nur auf Ressourcen ihrer zugewiesenen Tenants zugreifen
- Tenant-IDs werden aus dem JWT extrahiert
- ADMIN muss Tenant-IDs explizit angeben

---

## 4. GraphQL API

### 4.1 Queries

#### `alleNoten`
Gibt alle Noten im System zurück (nur Prüfungsamt/Admin).
```graphql
query {
  alleNoten {
    id
    matrikelnummer
    modulId
    modulName
    note
    status
  }
}
```

#### `notenVonStudent`
Gibt alle PUBLISHED Noten eines Studierenden zurück.
```graphql
query {
  notenVonStudent(matrikelnummer: "123456", tenantIds: ["uuid..."]) {
    id
    modulName
    note
    status
  }
}
```

#### `notenVonLehrenderNachModulUndStatus`
Filtert Noten nach Lehrendem, Modul, Status und Tenant.
```graphql
query {
  notenVonLehrenderNachModulUndStatus(
    lehrendenMatrikelnummer: "654321"
    tenantIds: ["uuid..."]
    modulId: 1001
    matrikelnummer: "123456"
  ) {
    id
    matrikelnummer
    note
    status
  }
}
```

#### `notenPruefungsamtZuValidieren`
Gibt TO_VALIDATE-Noten für Prüfungsamt mit optionalen Filtern zurück.
```graphql
query {
  notenPruefungsamtZuValidieren(
    tenantIds: ["uuid..."]
    modulId: 1001
    studiengang: "INF"
  ) {
    id
    matrikelnummer
    modulName
    note
  }
}
```

#### `alleModule`
Gibt alle Module für die angegebenen Tenants zurück.
```graphql
query {
  alleModule(tenantIds: ["uuid..."]) {
    modulId
    modulName
    ects
    studiengang
  }
}
```

#### `studentInfo`
Gibt Informationen über einen Studierenden inkl. Noten zurück.
```graphql
query {
  studentInfo(matrikelnummer: "123456") {
    matrikelnummer
    firstName
    lastName
    noten {
      modulName
      note
      status
    }
    anzahlBestandeneModule
  }
}
```

### 4.2 Mutations

#### `noteAnlegen`
Erstellt eine neue Note mit Status ADD (Prüfungsamt/Admin).
```graphql
mutation {
  noteAnlegen(input: {
    matrikelnummer: "123456"
    tenantId: "uuid..."
    modulId: 1001
    lehrendenMatrikelnummer: "654321"
    studiengang: "INF"
    semester: "WS2024"
  }) {
    id
    status
  }
}
```

#### `noteSetzen`
Setzt den Notenwert und ändert Status zu TO_VALIDATE (Lehrender).
```graphql
mutation {
  noteSetzen(input: {
    noteIds: ["uuid1", "uuid2"]
    note: 2.3
  }) {
    id
    note
    status
  }
}
```

#### `notenValidieren`
Validiert Noten und ändert Status zu PUBLISHED (Prüfungsamt).
```graphql
mutation {
  notenValidieren(input: {
    noteIds: ["uuid1", "uuid2"]
  }) {
    id
    status
  }
}
```

#### `modulErstellen`
Erstellt ein neues Modul (Prüfungsamt/Admin).
```graphql
mutation {
  modulErstellen(input: {
    modulId: 1001
    tenantId: "uuid..."
    modulName: "Web Engineering"
    ects: 6
    gewichtung: 1.0
    studiengang: "INF"
    semester: 3
    beschreibung: "Grundlagen Web-Entwicklung"
  }) {
    id
    modulId
    modulName
  }
}
```

---

## 5. Authentifizierung

### 5.1 JWT-Verarbeitung
Der Service liest JWT-Claims aus HTTP-Headers, die vom Gateway gesetzt werden:

| HTTP Header | JWT Claim | Verwendung |
|-------------|-----------|------------|
| `X-User-Id` | `sub` | Benutzeridentifikation |
| `X-User-Email` | `email` | E-Mail-Adresse |
| `X-User-Roles` | `role` | Rollenprüfung |
| `X-User-Tenant-Ids` | `tenant_ids` | Tenant-Isolation |
| `X-User-Matrikelnummer` | `matrikelnummer` | Studentenidentifikation |

### 5.2 JwtGraphQlInterceptor
Ein GraphQL-Interceptor extrahiert die JWT-Informationen aus den Headers und stellt sie als `JwtUser`-Objekt im GraphQL-Kontext bereit.

---

## 6. Fehlerbehandlung

### 6.1 Exceptions
| Exception | HTTP-Code | Beschreibung |
|-----------|-----------|--------------|
| `AuthenticationException` | 401 | Nicht authentifiziert |
| `AccessDeniedException` | 403 | Zugriff verweigert |
| `ValidationException` | 400 | Validierungsfehler |
| `NotFoundException` | 404 | Ressource nicht gefunden |

### 6.2 GraphQL-Fehlerantwort
```json
{
  "errors": [
    {
      "message": "Zugriff verweigert: Rolle nicht erlaubt",
      "extensions": {
        "classification": "FORBIDDEN"
      }
    }
  ],
  "data": null
}
```

---

## 7. Konfiguration

### 7.1 application.yaml
```yaml
server:
  port: 8082

spring:
  application:
    name: noten-modul-service
  datasource:
    url: jdbc:h2:mem:notendb
  jpa:
    hibernate:
      ddl-auto: create-drop
  graphql:
    graphiql:
      enabled: true
      path: /graphiql
    path: /graphql

jwt:
  secret: ${JWT_SECRET:...}

user-service:
  base-url: ${USER_SERVICE_BASE_URL:http://localhost:8081}
```

### 7.2 Umgebungsvariablen
| Variable | Beschreibung | Default |
|----------|--------------|---------|
| `JWT_SECRET` | Secret für JWT-Signatur | (dev-secret) |
| `USER_SERVICE_BASE_URL` | URL des User-Service | http://localhost:8081 |
| `H2_CONSOLE_ENABLED` | H2 Console aktivieren | false |
| `GRAPHIQL_ENABLED` | GraphiQL UI aktivieren | true |

---

## 8. Projektstruktur

```
noten-modulverwaltung-service/
├── src/main/java/de/frauas/notenmodulservice/
│   ├── client/                    # Externe Service-Clients
│   ├── config/                    # Spring-Konfiguration
│   ├── controller/
│   │   ├── QueryController.java   # GraphQL Queries
│   │   └── MutationController.java # GraphQL Mutations
│   ├── dto/
│   │   ├── ModulErstellenInput.java
│   │   ├── NoteErstellenInput.java
│   │   ├── NoteSetzenInput.java
│   │   ├── NoteValidierenInput.java
│   │   └── StudentDTO.java
│   ├── exception/                 # Custom Exceptions
│   ├── model/
│   │   ├── Note.java              # Note Entity
│   │   ├── Modul.java             # Modul Entity
│   │   └── NotenStatus.java       # Status Enum
│   ├── repository/
│   │   ├── NoteRepository.java
│   │   └── ModulRepository.java
│   ├── security/
│   │   ├── AccessControlService.java  # Zugriffskontrolle
│   │   ├── JwtGraphQlInterceptor.java # JWT-Extraktion
│   │   ├── JwtUser.java               # JWT-Benutzer
│   │   └── Role.java                  # Rollen-Enum
│   ├── service/
│   │   ├── NotenService.java      # Noten-Geschäftslogik
│   │   ├── ModulService.java      # Modul-Geschäftslogik
│   │   └── StudentService.java    # Studentendaten-Abruf
│   └── NotenModulServiceApplication.java
├── src/main/resources/
│   ├── application.yaml
│   └── graphql/
│       └── schema.graphqls        # GraphQL Schema
├── Dockerfile
└── pom.xml
```

---

## 9. Lokaler Start

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

### Endpunkte
| Endpunkt | URL |
|----------|-----|
| GraphQL API | http://localhost:8082/graphql |
| GraphiQL UI | http://localhost:8082/graphiql |
| H2 Console | http://localhost:8082/h2-console (wenn aktiviert) |

---

## 10. Tests

### Unit-Tests ausführen
```powershell
# Windows
.\mvnw.cmd test

# macOS/Linux
./mvnw test
```

### Build
```powershell
# Windows
.\mvnw.cmd clean package

# macOS/Linux
./mvnw clean package
```

---

## 11. Hinweis zur Erstellung

Bei der Erstellung dieser Dokumentation wurde KI-Unterstützung für den Feinschliff und die Vereinheitlichung der Formatierung verwendet. Die fachlichen Inhalte, Architekturentscheidungen und technische Spezifikation wurden eigenständig erarbeitet.

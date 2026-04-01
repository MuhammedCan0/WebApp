# Qualitätssicherung (QS) - Kriterienkatalog

## Projektziel
Dieses Dokument definiert die Qualitätskriterien für die Hochschul-Verwaltungssoftware mit Microservices-Architektur.

---

## 1. Funktionale Qualitätskriterien

### 1.1 Korrektheit
| ID | Kriterium | Prüfmethode | Akzeptanzkriterium |
|----|-----------|-------------|-------------------|
| F-01 | Notenberechnung entspricht PO-Vorgaben | Unit-Tests, Integrationstests | 100% der Testfälle bestanden |
| F-02 | Rollenbasierte Zugriffskontrolle funktioniert | Manuelle Tests, Automatisierte Tests | Nur autorisierte Nutzer haben Zugriff |
| F-03 | API-Gateway routet korrekt zu Services | Integrationstests | Alle Endpunkte erreichbar |
| F-04 | Mandantentrennung gewährleistet | Sicherheitstests | Keine Cross-Tenant Datenzugriffe |
| F-05 | CRUD-Operationen für Module/Noten funktionieren | API-Tests | Alle Operationen erfolgreich |

### 1.2 Vollständigkeit
| ID | Kriterium | Prüfmethode | Akzeptanzkriterium |
|----|-----------|-------------|-------------------|
| V-01 | Alle geplanten Microservices implementiert | Code-Review | Gateway, Auth, Noten, Modulverwaltung |
| V-02 | REST-Schnittstellen dokumentiert | Swagger/OpenAPI | 100% Dokumentationsabdeckung |
| V-03 | GraphQL-Schema vollständig | Schema-Validierung | Alle Queries/Mutations definiert |

---
## 2. Nicht-Funktionale Qualitätskriterien

### 2.1 Performance
| ID | Kriterium | Prüfmethode | Akzeptanzkriterium |
|----|-----------|-------------|-------------------|
| P-01 | API-Antwortzeit | Lasttests | < 500ms für 95% der Requests |
| P-02 | Gleichzeitige Benutzer | Lasttests | Min. 50 gleichzeitige Nutzer |
| P-03 | Datenbankabfragen optimiert | Query-Analyse | Keine N+1 Probleme |

### 2.2 Sicherheit
| ID | Kriterium | Prüfmethode | Akzeptanzkriterium |
|----|-----------|-------------|-------------------|
| S-01 | JWT-Authentifizierung implementiert | Security-Tests | Gültige Tokens erforderlich |
| S-02 | Passwörter verschlüsselt gespeichert | Code-Review | BCrypt oder äquivalent |
| S-03 | SQL-Injection verhindert | Security-Scan | Keine Vulnerabilities |
| S-04 | CORS korrekt konfiguriert | Manuelle Tests | Nur erlaubte Origins |
| S-05 | Sensitive Daten nicht in Logs | Log-Analyse | Keine Passwörter/Tokens geloggt |

### 2.3 Zuverlässigkeit
| ID | Kriterium | Prüfmethode | Akzeptanzkriterium |
|----|-----------|-------------|-------------------|
| Z-01 | Service-Health-Checks | Monitoring | Alle Services erreichbar |
| Z-02 | Fehlerbehandlung implementiert | Tests | Graceful Error Handling |
| Z-03 | Transaktionssicherheit | Integrationstests | ACID-Eigenschaften erfüllt |

### 2.4 Wartbarkeit
| ID | Kriterium | Prüfmethode | Akzeptanzkriterium |
|----|-----------|-------------|-------------------|
| W-01 | Code-Dokumentation | Code-Review | Javadoc für öffentliche Methoden |
| W-02 | Einheitlicher Code-Stil | Linter/Formatter | Checkstyle/SpotBugs ohne Fehler |
| W-03 | Modulare Architektur | Architektur-Review | Lose Kopplung zwischen Services |
| W-04 | Logging implementiert | Code-Review | Strukturierte Logs vorhanden |

### 2.5 Testabdeckung
| ID | Kriterium | Prüfmethode | Akzeptanzkriterium |
|----|-----------|-------------|-------------------|
| T-01 | Unit-Test-Abdeckung | JaCoCo | Min. 70% Line Coverage |
| T-02 | Integrationstests vorhanden | Test-Review | Kritische Pfade abgedeckt |
| T-03 | API-Tests (Postman/etc.) | Test-Ausführung | Alle Endpunkte getestet |

---

## 3. Architektur-Qualitätskriterien

### 3.1 Microservices-Architektur
| ID | Kriterium | Prüfmethode | Akzeptanzkriterium |
|----|-----------|-------------|-------------------|
| A-01 | Service-Unabhängigkeit | Architektur-Review | Services einzeln deploybar |
| A-02 | API-Gateway funktional | Integrationstests | Zentrales Routing funktioniert |
| A-03 | Ereignisbasierte Kommunikation | Code-Review | Message Queue/Event Bus vorhanden |
| A-04 | Datenbank pro Service | Architektur-Review | Keine geteilten Datenbanken |

### 3.2 Skalierbarkeit
| ID | Kriterium | Prüfmethode | Akzeptanzkriterium |
|----|-----------|-------------|-------------------|
| SK-01 | Horizontale Skalierung möglich | Architektur-Review | Stateless Services |
| SK-02 | Container-ready | Docker-Test | Dockerfile vorhanden & funktional |

---

## 4. Dokumentations-Qualitätskriterien

| ID | Kriterium | Prüfmethode | Akzeptanzkriterium |
|----|-----------|-------------|-------------------|
| D-01 | README vollständig | Review | Setup-Anleitung vorhanden |
| D-02 | API-Dokumentation aktuell | Vergleich Code/Docs | Keine Diskrepanzen |
| D-03 | Architekturdiagramme vorhanden | Review | System- & Sequenzdiagramme |
| D-04 | Installationsanleitung | Test | Reproduzierbare Installation |

---

## 5. Checkliste für Abnahme

### Pre-Release Checkliste
- [ ] Alle Unit-Tests bestanden
- [ ] Alle Integrationstests bestanden
- [ ] Code-Review durchgeführt
- [ ] Keine kritischen Security-Issues
- [ ] Performance-Anforderungen erfüllt
- [ ] Dokumentation vollständig
- [ ] Docker-Container funktionsfähig

### Service-spezifische Checklisten

#### Gateway-Service
- [ ] Routing zu allen Services funktioniert
- [ ] JWT-Validierung aktiv
- [ ] Rate-Limiting konfiguriert
- [ ] CORS konfiguriert

#### User-Tenant-Auth-Service
**Authentifizierung & Token**
- [ ] Benutzerregistrierung funktioniert
- [ ] Login/Logout funktioniert
- [ ] JWT-Token-Generierung korrekt
- [ ] JWT-Token-Validierung funktioniert
- [ ] Token-Refresh-Mechanismus implementiert

**Benutzerverwaltung**
- [ ] Rollen-Management implementiert
- [ ] Benutzer-Status-Management (ACTIVE/INACTIVE) funktioniert
- [ ] User-Aktivierung/Deaktivierung möglich
- [ ] User-Update mit Validierungen funktioniert
- [ ] User-Löschung funktioniert
- [ ] Email-Eindeutigkeit gewährleistet (keine Duplikate)
- [ ] Matrikelnummern-Generierung korrekt

**Multi-Tenant-Support**
- [ ] Mandantentrennung gewährleistet
- [ ] Multi-Tenant-User-Zuordnung funktioniert
- [ ] Cross-Tenant-Datenzugriffe sind ausgeschlossen
- [ ] Tenant-Service-Integration (Fachbereich-Auflösung) funktioniert
- [ ] Auto-Tenant-Resolution über Studiengänge funktioniert

**Multi-Studiengang-Support**
- [ ] Multi-Studiengang-Zuordnung zu Users funktioniert
- [ ] Studiengang-Validierung gegen Tenant durchgeführt
- [ ] Backward-Compatibility für Einzelstudiengang erhalten

**Rollenspezifische Validierungen**
- [ ] STUDENT-Rolle: mindestens 1 Studiengang erforderlich
- [ ] STUDENT-Rolle: Studiengänge validiert gegen Tenants
- [ ] PRUEFUNGSAMT-Rolle: genau 1 Tenant erzwungen
- [ ] PRUEFUNGSAMT-Rolle: Studiengang optional validiert
- [ ] LEHRENDER-Rolle: mehrere Tenants erlaubt
- [ ] LEHRENDER-Rolle: keine Studiengänge erforderlich
- [ ] ADMIN-Rolle: keine Tenant/Studiengang-Anforderungen

**Sicherheit**
- [ ] Passwörter mit BCrypt verschlüsselt
- [ ] Sensitive Daten (Passwörter, Tokens) nicht in Logs
- [ ] Rollenbasierte Zugriffskontrolle (RBAC) korrekt implementiert
- [ ] CORS korrekt konfiguriert für Gateway

**API & Schnittstellen**
- [ ] REST-Endpoints dokumentiert (Swagger/OpenAPI)
- [ ] User CRUD-Operationen funktionieren (Create, Read, Update, Delete)
- [ ] Filterung nach Tenant/Rolle/Status funktioniert
- [ ] Error-Responses konsistent (HTTP-Status-Codes)
- [ ] Validierungsfehler aussagekräftig

#### Noten-Modulverwaltung-Service
- [ ] Module anlegen/bearbeiten/löschen
- [ ] GraphQL-API funktional
- [ ] Validierungen implementiert
- [ ] PO-Zuordnung möglich

#### Notenberechnung-Service
- [ ] Gewichtete Notenberechnung korrekt
- [ ] PO-konforme Berechnung
- [ ] Fehlerbehandlung für ungültige Eingaben
- [ ] API-Responses konsistent

---

## 6. GitHub Labels für Issues

Empfohlene Labels zur Qualitätssicherung:

| Label | Farbe | Beschreibung |
|-------|-------|--------------|
| `qa:bug` | #d73a4a | Fehler/Bug gefunden |
| `qa:testing` | #0e8a16 | Benötigt Tests |
| `qa:security` | #b60205 | Sicherheitsrelevant |
| `qa:performance` | #fbca04 | Performance-Issue |
| `qa:documentation` | #0075ca | Dokumentation fehlt/fehlerhaft |
| `qa:code-review` | #5319e7 | Code-Review erforderlich |
| `qa:passed` | #2ea44f | QS-Prüfung bestanden |
| `qa:blocked` | #e99695 | Blockiert durch QS-Issue |

---
## 7. Testplan-Übersicht

```
┌─────────────────────────────────────────────────────────────┐
│                    TESTPYRAMIDE                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                    ┌─────────┐                              │
│                    │   E2E   │  ← Manuelle/UI-Tests         │
│                  ┌─┴─────────┴─┐                            │
│                  │ Integration │  ← API-Tests, Service-     │
│                  │    Tests    │    Kommunikation           │
│                ┌─┴─────────────┴─┐                          │
│                │   Unit Tests    │  ← Geschäftslogik,       │
│                │                 │    Berechnungen          │
│                └─────────────────┘                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```




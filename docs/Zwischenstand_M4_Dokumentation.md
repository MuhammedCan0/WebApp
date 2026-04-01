# Zwischenstand Meilenstein 4 - Qualitätssicherung

**Stand:** 13. Februar 2026  
**Projektname:** Hochschul-Verwaltungssoftware (webapp-404-brain-not-found)  
**Team:** 404 Brain Not Found

---

## Zielbeschreibung

Durchführung umfassender Qualitätssicherungsmaßnahmen für alle Microservices der Hochschul-Verwaltungssoftware. Dies umfasst die Definition von QS-Kriterien, die Erstellung von Postman-Collections zur API-Testautomatisierung sowie die Durchführung von Systemtests und Bugfixing.

---

## Erwartetes Ergebnis

- **QS-Kriterienkatalog** mit funktionalen und nicht-funktionalen Anforderungen
- **Vollständige Postman-Collection** zur Qualitätsprüfung aller Service-Endpunkte
- **Automatisierte Test-Scripts** mit Validierungen in der Postman-Collection
- **Dokumentierte Testergebnisse** und identifizierte Bugs
- **Behobene kritische Fehler** aus den Systemtests
- **Testabdeckung** von mindestens 70% für kritische Pfade

---

## Übersicht der Tasks

| Task | Beschreibung | Status | Fortschritt |
|------|--------------|--------|-------------|
| **4.1** | Anforderungen der Qualitätssicherung bestimmen | ✅ Abgeschlossen | 100% |
| **4.1.1** | Postman-Collection für alle Services erstellen | ✅ Abgeschlossen | 100% |
| **4.2** | Systemtests & Bugfixing durchführen | 🔄 In Bearbeitung | ~50% |
| **4.3** | Zwischenstand dokumentieren | 🔄 In Bearbeitung | 50% |

---

## 4.1 - Anforderungen der Qualitätssicherung bestimmen ✅

**Erstellte Dokumente:**

### QUALITY_ASSURANCE.md
Umfassender Kriterienkatalog mit:
- Funktionale Qualitätskriterien (Korrektheit, Vollständigkeit)
- Nicht-funktionale Kriterien (Performance, Sicherheit, Wartbarkeit)
- Architektur-Qualitätskriterien
- Dokumentations-Anforderungen
- Testplan-Übersicht (Testpyramide)

### QS_CHECKLIST.md
Schnellübersicht für:
- Pre-Merge/Release Checklisten
- Service-spezifische Tests
- Ergebnis-Protokollierung

---

## 4.1.1 - Postman-Collection erstellen ✅

### Erstellte Collections

| Collection | Speicherort | Beschreibung |
|------------|-------------|--------------|
| **Zentrale QS-Collection** | `docs/postman/Hochschul-Verwaltung-API.postman_collection.json` | Vollständige Test-Suite |
| **Environment** | `docs/postman/Hochschul-Verwaltung-Environment.postman_environment.json` | Konfiguration |
| User-Tenant-Auth | `user-tenant-auth-service/docs/` | Service-spezifisch |
| Noten-Modulverwaltung | `noten-modulverwaltung-service/` | GraphQL-Tests |
| Notenberechnung | `notenberechnung-service/.../tools/postman/` | REST-Tests |

### Abgedeckte Testbereiche

**Authentifizierung:**
- ✅ Login für alle Rollen (Admin, Prüfungsamt, Student, Lehrender)
- ✅ Token-Management mit automatischer Speicherung
- ✅ Rollenbasierte Zugriffstests

**Multi-Tenant:**
- ✅ Mandantentrennung
- ✅ Cross-Tenant-Schutz

**APIs:**
- ✅ GraphQL-Queries und Mutations (Noten-Modulverwaltung)
- ✅ REST-API-Endpunkte (Auth, Notenberechnung)
- ✅ Gateway-Routing

**Automatisierung:**
- ✅ JavaScript Test-Scripts
- ✅ Automatische Token-Extraktion
- ✅ Variablen-Management

---

## 4.2 - Systemtests & Bugfixing 🔄

**Status:** In Bearbeitung

### Durchgeführte Tests

| Testbereich | Status | Anmerkungen |
|-------------|--------|-------------|
| Unit-Tests | 🔄 | `./mvnw test` pro Service |
| Integrationstests | 🔄 | Service-zu-Service |
| Postman-Collection | 🔄 | Durchlauf ausstehend |
| Sicherheitstests | ⏳ | JWT, CORS, Input-Validierung |
| Performance-Tests | ⏳ | Lasttests geplant |

### Bekannte Issues

| Issue | Service | Priorität | Status |
|-------|---------|-----------|--------|
| - | - | - | - |

*Tabelle wird nach Testdurchlauf gefüllt*

---

## 4.3 - Zwischenstand dokumentieren 🔄

**Status:** In Bearbeitung (dieses Dokument)

---

## Nächste Schritte

1. **Systemtests abschließen**
   - Alle 4 Services gleichzeitig starten
   - Postman-Collection vollständig durchlaufen
   - Testergebnisse protokollieren

2. **Bugfixing**
   - Gefundene Fehler in GitHub Issues dokumentieren
   - Fixes implementieren und testen

3. **Test-Coverage prüfen**
   - JaCoCo-Reports generieren
   - Ziel: ≥70% Line Coverage

4. **Dokumentation finalisieren**
   - Testergebnisse in QS_CHECKLIST.md eintragen
   - Abschlussbericht erstellen

---

## Risiken & Blocker

| Risiko | Auswirkung | Maßnahme |
|--------|------------|----------|
| Services nicht gleichzeitig startbar | Tests nicht ausführbar | Docker-Compose Setup nutzen |
| Fehlende Testdaten | Unvollständige Tests | Seed-Daten in Collection |
| Port-Konflikte | Services starten nicht | Ports prüfen (8080-8083) |

---

## Abhängigkeiten

Task 4.1.1 war abhängig von:
- ✅ 2.2 - User-/Rollen-/Auth-Service (#18)
- ✅ 2.3 - Noten-/Modul-Verwaltung (#19)
- ✅ 2.4 - Notenberechnung (#21)
- ✅ 3.2 - API-Gateway (#9)
- ✅ 4.1 - QS-Anforderungen (#8)

---

## Verantwortlichkeiten

| Task | Zuständig | Vertretung |
|------|-----------|------------|
| 4.1 | Alle | Alle |
| 4.1.1 | Alle | Alle |
| 4.2 | Alle | Alle |
| 4.3 | Alle | Alle |

---

**Nächster Meilenstein:** 5 - Dokumentation & Abschluss

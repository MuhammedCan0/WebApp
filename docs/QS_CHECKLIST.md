# QS-Checkliste - Schnellübersicht

## Vor jedem Merge/Release prüfen:

### ✅ Code-Qualität
- [ ] Code kompiliert ohne Fehler
- [ ] Keine Compiler-Warnings
- [ ] Checkstyle/Linter bestanden
- [ ] Code-Review durchgeführt (min. 1 Reviewer)

### ✅ Tests
- [ ] Unit-Tests: `./mvnw test` erfolgreich
- [ ] Integrationstests bestanden
- [ ] Testabdeckung ≥ 70%
- [ ] Manuelle Smoke-Tests durchgeführt

### ✅ Sicherheit
- [ ] Keine hartkodierten Credentials
- [ ] JWT-Validierung funktioniert
- [ ] Eingabevalidierung vorhanden
- [ ] Keine SQL-Injection möglich

### ✅ API
- [ ] Endpunkte erreichbar
- [ ] Responses im korrekten Format
- [ ] Fehler-Responses konsistent
- [ ] Swagger/OpenAPI aktuell

### ✅ Dokumentation
- [ ] README aktualisiert
- [ ] API-Änderungen dokumentiert
- [ ] Code-Kommentare vorhanden

### ✅ Deployment
- [ ] Docker-Build erfolgreich
- [ ] Alle Services starten
- [ ] Health-Checks grün

---

## Service-spezifische Schnelltests

### Gateway-Service
```bash
# Health-Check
curl http://localhost:8080/actuator/health

# Routing-Test
curl http://localhost:8080/api/grades/calculate
```

### Auth-Service
```bash
# Login-Test
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

### Notenberechnung-Service
```bash
# Berechnung-Test
curl -X POST http://localhost:8082/api/grades/calculate \
  -H "Content-Type: application/json" \
  -d '{"grades":[{"moduleId":"MATH101","grade":1.7}]}'
```

---

## Ergebnis-Protokoll

| Datum | Prüfer | Ergebnis | Anmerkungen |
|-------|--------|----------|-------------|
| | | ☐ Bestanden / ☐ Nicht bestanden | |
| | | ☐ Bestanden / ☐ Nicht bestanden | |
| | | ☐ Bestanden / ☐ Nicht bestanden | |

---

*Verwende [QUALITY_ASSURANCE.md](QUALITY_ASSURANCE.md) für detaillierte Kriterien.*

# Test-Ergebnisse: Noten-Modulverwaltung-Service Postman Collection

## ✅ Erfolgreich getestet

### 1. Authentication (Setup)
Alle Login-Endpoints funktionieren einwandfrei:

- ✅ **Admin Login** - Token erfolgreich erhalten
  - Email: `admin@university.de`
  - Token: `eyJhbGciOiJIUzI1NiJ9...` (JWT gültig)

- ✅ **Prüfungsamt Login** - Token erfolgreich erhalten
  - Email: `pruefungsamt@university.de`
  
- ✅ **Lehrender Login** - Token erfolgreich erhalten
  - Email: `lehrender@university.de`
  
- ✅ **Student Login** - Token erfolgreich erhalten
  - Email: `student@university.de`

**Alle Tokens wurden automatisch in Collection Variables gespeichert!**

---

## 📋 Verwendung der Postman Collection

### Import in Postman
1. Öffne Postman
2. Klicke auf **Import**
3. Wähle die Datei: 
   ```
   c:\Users\huhji\Desktop\webapp-404-brain-not-found\noten-modulverwaltung-service\noten-modulverwaltung-service.postman_collection.json
   ```

### Services starten

**Terminal 1 - Auth-Service:**
```powershell
cd c:\Users\huhji\Desktop\webapp-404-brain-not-found\user-tenant-auth-service
.\mvnw spring-boot:run
```
Warte bis du siehst: `Started AuthServiceApplication`

**Terminal 2 - Noten-Service:**
```powershell
cd c:\Users\huhji\Desktop\webapp-404-brain-not-found\noten-modulverwaltung-service
.\mvnw spring-boot:run
```
Warte bis du siehst: `Started NotenModulServiceApplication`

### Test-Ablauf in Postman

#### Schritt 1: Authentication (ZUERST!)
Führe alle 4 Login-Requests aus:
```
0. Setup - Authentication
  → Login as Admin
  → Login as Prüfungsamt  
  → Login as Lehrender
  → Login as Student
```
✅ Tokens werden automatisch gespeichert!

#### Schritt 2: Basis-Tests
```
1. Noten Queries
  → Alle Noten (Admin/Prüfungsamt)
  → Noten von Student (mit Matrikelnummer)
  → Student Info mit Noten

3. Modul Queries
  → Alle Module
  → Module nach Studiengang
```

#### Schritt 3: Vollständiger Workflow
```
5. Test-Workflows
  → Workflow: Note erstellen → setzen → validieren
    1. Note anlegen (Prüfungsamt)     → Status: ADD
    2. Note setzen (Lehrender)        → Status: TO_VALIDATE
    3. Note validieren (Prüfungsamt)  → Status: PUBLISHED
    4. Verifizierung                  → Note ist sichtbar!
```

#### Schritt 4: Negative Tests
```
6. Negative Tests
  → Unauthorized - Kein Token           → 401 Fehler
  → Student versucht Note anzulegen     → 403 Forbidden
  → Ungültige Note (außerhalb 1.0-5.0)  → Validation Error
```

---

## 🎯 Beispiel-Responses

### Query: Alle Module
```json
{
  "data": {
    "alleModule": [
      {
        "id": "uuid-...",
        "modulId": 1,
        "modulName": "Grundlagen der Informatik",
        "ects": 5,
        "gewichtung": 0.057,
        "studiengang": "Luftverkehrsmanagement_-_Aviation_Management_dual_BA",
        "semester": 1
      },
      ...
    ]
  }
}
```

### Mutation: Note anlegen
```json
{
  "data": {
    "noteAnlegen": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "matrikelnummer": "1525326",
      "modulId": 1,
      "modulName": "Grundlagen der Informatik",
      "status": "ADD",
      "lehrendenMatrikelnummer": "L001",
      "erstelltAm": "2026-02-08T20:57:14Z"
    }
  }
}
```

### Mutation: Note setzen (Lehrender)
```json
{
  "data": {
    "noteSetzen": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "note": 1.7,
        "status": "TO_VALIDATE",
        "aktualisiertAm": "2026-02-08T21:05:30Z"
      }
    ]
  }
}
```

---

## 💡 Tipps für Postman

### Variables überprüfen
- Klicke auf die Collection → Tab **Variables**
- Prüfe ob nach Login folgende gesetzt sind:
  - `admin_token`
  - `pruefungsamt_token`
  - `lehrender_token`
  - `student_token`
  - `test_note_id` (nach Note anlegen)

### Console für Debugging
- Postman → unten links **Console** öffnen
- Siehst du alle Requests, Headers und Responses
- Hilft bei Fehlersuche

### Auto-Scripts
Die Collection enthält automatische Scripts:
- **Pre-request Scripts**: Bereiten Daten vor
- **Test Scripts**: Speichern Tokens und IDs automatisch
- **Logging**: Console-Output für jeden Schritt

### GraphiQL Alternative
Statt Postman kannst du auch den Browser nutzen:
```
http://localhost:8082/graphiql
```
- Interaktives GraphQL UI
- Auto-Completion (Ctrl+Space)
- Schema-Explorer
- Kopiere Queries aus der Postman Collection

---

## 🔧 Troubleshooting

### "Connection refused" Fehler
- Services noch nicht gestartet → Warte 30 Sekunden nach Start
- Port bereits belegt → Prüfe mit `netstat -ano | findstr "8082"`

### "Unauthorized" (401)
- Token nicht gesetzt → Führe "0. Setup - Authentication" zuerst aus
- Token abgelaufen → Hole neuen Token (Login erneut ausführen)

### "Forbidden" (403)
- Falsche Rolle → Prüfe ob richtiger Token verwendet wird
  - Studenten können keine Noten anlegen
  - Nur Prüfungsamt kann validieren

### GraphQL Errors
- Syntax-Fehler in Query → Prüfe Anführungszeichen und Kommas
- Fehlende Required-Felder → Schema prüfen
- Ungültige Note-Werte → Nur 1.0 - 5.0 erlaubt

---

## 📊 Collection-Statistik

- **Total Requests**: 28
- **Folder**: 7
- **Queries**: 11
- **Mutations**: 10
- **Workflows**: 2 (vollständig)
- **Negative Tests**: 4

---

## 🚀 Nächste Schritte

1. ✅ Import Collection in Postman
2. ✅ Services starten (beide Terminals)
3. ✅ Authentication durchführen
4. ✅ Basis-Queries testen
5. ✅ Vollständigen Workflow ausführen
6. ✅ Eigene Tests erstellen

**Viel Erfolg beim Testen! 🎉**

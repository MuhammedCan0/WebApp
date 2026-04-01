# GraphQL Query Beispiele für Tests

Kopiere diese Queries in GraphiQL (http://localhost:8082/graphiql) um den Service zu testen.

## 🔍 QUERIES (Lesezugriffe)

### 1. Alle Noten eines Studierenden
```graphql
query NotenEinesStudenten {
  notenVonStudent(matrikelnummer: "1507519") {
    id
    modulId
    modulName
    note
    status
    lehrender
    studiengang
    semester
    erstelltAm
    aktualisiertAm
  }
}
```

### 2. Alle veröffentlichten Noten
```graphql
query VeroeffentlichteNoten {
  notenNachStatus(status: PUBLISHED) {
    id
    matrikelnummer
    modulName
    note
    status
    lehrender
  }
}
```

### 3. Alle Module des Studiengangs Luftverkehrsmanagement
```graphql
query ModuleLuftverkehr {
  moduleNachStudiengang(studiengang: "Luftverkehrsmanagement_-_Aviation_Management_dual_BA") {
    id
    modulId
    modulName
    ects
    gewichtung
    semester
    beschreibung
  }
}
```

### 4. Studenteninformationen mit allen Noten
```graphql
query StudentMitNoten {
  studentInfo(matrikelnummer: "1507519") {
    matrikelnummer
    firstName
    lastName
    studiengang
    semester
    noten {
      id
      modulName
      note
      status
      lehrender
    }
  }
}
```

### 5. Alle Noten eines Lehrenden
```graphql
query NotenVonLehrendem {
  notenVonLehrender(lehrender: "Prof. Dr. Müller") {
    id
    matrikelnummer
    modulName
    note
    status
    semester
  }
}
```

### 6. Alle Noten die validiert werden müssen
```graphql
query NotenZuValidieren {
  notenNachStatus(status: TO_VALIDATE) {
    id
    matrikelnummer
    modulName
    lehrender
    erstelltAm
  }
}
```

### 7. Einzelne Note abrufen
```graphql
query EinzelneNote {
  noteById(id: "HIER-UUID-EINFÜGEN") {
    id
    matrikelnummer
    modulId
    modulName
    note
    status
    lehrender
    studiengang
    semester
    erstelltAm
    aktualisiertAm
  }
}
```

### 8. Alle Module abrufen
```graphql
query AlleModule {
  alleModule {
    id
    modulId
    modulName
    ects
    gewichtung
    studiengang
    semester
  }
}
```

### 9. Einzelnes Modul abrufen
```graphql
query EinzelnesModul {
  modulById(modulId: "1") {
    id
    modulId
    modulName
    ects
    gewichtung
    studiengang
    semester
    beschreibung
  }
}
```

## ✏️ MUTATIONS (Schreibzugriffe)

### 1. Neue Note anlegen (Prüfungsamt)
```graphql
mutation NeueNoteAnlegen {
  noteAnlegen(input: {
    matrikelnummer: "1525326"
    modulId: "2"
    lehrender: "Prof. Dr. Schmidt"
    studiengang: "Luftverkehrsmanagement_-_Aviation_Management_dual_BA"
    semester: "1"
  }) {
    id
    matrikelnummer
    modulId
    modulName
    status
    lehrender
    erstelltAm
  }
}
```

### 2. Note setzen (Lehrender)
```graphql
mutation NotEintragen {
  noteSetzen(input: {
    noteIds: ["HIER-UUID-EINFÜGEN"]
    note: 1.7
  }) {
    id
    matrikelnummer
    modulName
    note
    status
    aktualisiertAm
  }
}
```

### 3. Mehrere Noten setzen (Lehrender)
```graphql
mutation MehrereNotenSetzen {
  noteSetzen(input: {
    noteIds: [
      "UUID-1",
      "UUID-2",
      "UUID-3"
    ]
    note: 2.3
  }) {
    id
    modulName
    note
    status
  }
}
```

### 4. Noten validieren (Prüfungsamt)
```graphql
mutation NotenFreigeben {
  notenValidieren(input: {
    noteIds: ["HIER-UUID-EINFÜGEN"]
  }) {
    id
    matrikelnummer
    modulName
    note
    status
    aktualisiertAm
  }
}
```

### 5. Mehrere Noten validieren (Prüfungsamt)
```graphql
mutation MehrereNotenValidieren {
  notenValidieren(input: {
    noteIds: [
      "UUID-1",
      "UUID-2"
    ]
  }) {
    id
    modulName
    note
    status
  }
}
```

### 6. Note löschen (Prüfungsamt)
```graphql
mutation NoteEntfernen {
  noteLoeschen(id: "HIER-UUID-EINFÜGEN")
}
```

### 7. Neues Modul erstellen (Prüfungsamt)
```graphql
mutation NeuesModul {
  modulErstellen(input: {
    modulId: "7"
    modulName: "Mikroökonomik"
    ects: 5
    gewichtung: 0.057
    studiengang: "Luftverkehrsmanagement_-_Aviation_Management_dual_BA"
    semester: 2
    beschreibung: "Grundlagen der Mikroökonomie und Markttheorie"
  }) {
    id
    modulId
    modulName
    ects
    gewichtung
    studiengang
  }
}
```

### 8. Modul aktualisieren (Prüfungsamt)
```graphql
mutation ModulBearbeiten {
  modulAktualisieren(
    modulId: "7"
    input: {
      modulId: "7"
      modulName: "Mikroökonomik und Markttheorie"
      ects: 5
      gewichtung: 0.057
      studiengang: "Luftverkehrsmanagement_-_Aviation_Management_dual_BA"
      semester: 2
      beschreibung: "Erweiterte Beschreibung"
    }
  ) {
    id
    modulId
    modulName
    beschreibung
  }
}
```

### 9. Modul löschen (Prüfungsamt)
```graphql
mutation ModulEntfernen {
  modulLoeschen(modulId: "7")
}
```

## 🔗 KOMPLEXE QUERIES

### 1. Vollständige Studentenübersicht mit allen Details
```graphql
query VollstaendigeStudentenansicht {
  studentInfo(matrikelnummer: "1507519") {
    matrikelnummer
    firstName
    lastName
    studiengang
    semester
    noten {
      id
      modulId
      modulName
      note
      status
      lehrender
      erstelltAm
      aktualisiertAm
    }
  }
}
```

### 2. Übersicht aller Noten für das Prüfungsamt
```graphql
query PruefungsamtUebersicht {
  alleNoten {
    id
    matrikelnummer
    modulId
    modulName
    note
    status
    lehrender
    studiengang
    semester
    erstelltAm
  }
}
```

### 3. Alle zu validierenden Noten mit Details
```graphql
query ZuValidierendeNotenMitDetails {
  notenNachStatus(status: TO_VALIDATE) {
    id
    matrikelnummer
    modulId
    modulName
    note
    lehrender
    studiengang
    erstelltAm
  }
}
```

## 🧪 TEST-WORKFLOW

### Kompletter Workflow: Note erstellen → setzen → validieren

**Schritt 1: Note anlegen (Prüfungsamt)**
```graphql
mutation {
  noteAnlegen(input: {
    matrikelnummer: "1449262"
    modulId: "1"
    lehrender: "Prof. Dr. Müller"
    studiengang: "Luftverkehrsmanagement_-_Aviation_Management_dual_BA"
    semester: "1"
  }) {
    id  # Diese ID für die nächsten Schritte notieren!
    status
  }
}
```

**Schritt 2: Note eintragen (Lehrender)**
```graphql
mutation {
  noteSetzen(input: {
    noteIds: ["ID-AUS-SCHRITT-1"]
    note: 1.3
  }) {
    id
    note
    status  # Sollte jetzt TO_VALIDATE sein
  }
}
```

**Schritt 3: Note validieren (Prüfungsamt)**
```graphql
mutation {
  notenValidieren(input: {
    noteIds: ["ID-AUS-SCHRITT-1"]
  }) {
    id
    note
    status  # Sollte jetzt PUBLISHED sein
  }
}
```

**Schritt 4: Verifizieren**
```graphql
query {
  notenVonStudent(matrikelnummer: "1449262") {
    id
    modulName
    note
    status
  }
}
```

## 💡 TIPPS

1. **UUIDs finden**: Führe zuerst eine Query aus, um die UUIDs zu sehen
2. **Status prüfen**: Verwende `notenNachStatus` um den aktuellen Status zu sehen
3. **Fehler debuggen**: Schau in die Browser-Konsole oder Server-Logs
4. **GraphiQL nutzen**: Drücke `Ctrl+Space` für Auto-Completion
5. **Schema erkunden**: Klicke auf "Docs" in GraphiQL um das komplette Schema zu sehen

## 🎯 ROLLENBASIERTE TEST-SZENARIEN

### Als Student
```graphql
# Nur eigene Noten abrufen
query {
  notenVonStudent(matrikelnummer: "1507519") {
    modulName
    note
    status
  }
}

# Module des eigenen Studiengangs ansehen
query {
  moduleNachStudiengang(studiengang: "Luftverkehrsmanagement_-_Aviation_Management_dual_BA") {
    modulName
    ects
  }
}
```

### Als Lehrender
```graphql
# Eigene Noten abrufen
query {
  notenVonLehrender(lehrender: "Prof. Dr. Müller") {
    matrikelnummer
    modulName
    note
    status
  }
}

# Noten eintragen
mutation {
  noteSetzen(input: {
    noteIds: ["UUID"]
    note: 2.0
  }) {
    id
    status
  }
}
```

### Als Prüfungsamt
```graphql
# Alle Noten sehen
query {
  alleNoten {
    matrikelnummer
    modulName
    note
    status
  }
}

# Noten validieren
mutation {
  notenValidieren(input: {
    noteIds: ["UUID1", "UUID2"]
  }) {
    id
    status
  }
}

# Module verwalten
mutation {
  modulErstellen(input: {
    modulId: "NEW_ID"
    modulName: "Neues Modul"
    ects: 5
    gewichtung: 0.1
    studiengang: "Luftverkehrsmanagement_-_Aviation_Management_dual_BA"
  }) {
    id
    modulName
  }
}
```
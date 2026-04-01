# User Stories - Hochschul-Verwaltungssoftware

 
**Projekt:** webapp-404-brain-not-found  
**Team:** 404 Brain Not Found

---

## Übersicht der Rollen

| Rolle | Beschreibung |
|-------|-------------|
| **Nutzer** | Allgemeine authentifizierte Benutzer |
| **Student** | Studierende mit Zugang zu eigenen Noten und Modulen |
| **Lehrende/r (Dozent)** | Lehrpersonal mit Noteneingabe-Berechtigung |
| **Prüfungsamt** | Verwaltung von Studierenden, Noten und Modulen im eigenen Fachbereich |
| **Administrator** | Systemweite Verwaltung und Mandanten-Management |

---

## 1. Authentifizierung & Profil (Nutzer)

| ID | Als... | möchte ich... | damit/weil/denn... |
|----|--------|---------------|-------------------|
| N-01 | Nutzer | mich mit E-Mail und Passwort einloggen | ich Zugriff auf meine persönlichen Funktionen im System erhalte. |
| N-02 | Nutzer | mein eigenes Profil aufrufen | ich meine persönlichen Daten und Rollen einsehen kann. |
| N-03 | Nutzer | beim Login meine Rollen, Token-Ablaufzeit und User-ID erhalten | ich weiß, welche Berechtigungen ich habe und wann ich mich neu anmelden muss. |


---

## 2. Studierenden-Funktionen (Student)

| ID | Als... | möchte ich... | damit/weil/denn... |
|----|--------|---------------|-------------------|
| S-01 | Student | meine zugeordneten Module und Noten einsehen | ich meine aktuellen Leistungsstände nachvollziehen kann. |
| S-02 | Student | nur die Module der Studiengänge sehen, in denen ich eingeschrieben bin | meine Ansicht korrekt und berechtigt ist. |
| S-03 | Student | bei mehreren Studiengängen die Module all meiner Studiengänge sehen | ich meine gesamte Studienleistung überblicken kann. |
| S-04 | Student | die automatisch berechnete Gesamtnote je Studiengang gemäß PO sehen | ich meine Abschlussrelevanz nachvollziehen kann. |
| S-05 | Student | meine Noten inkl. deren Status (z.B. vorläufig/validiert) einsehen | ich Klarheit über die Verbindlichkeit meiner Noten habe. |
| S-06 | Student | meinen gewichteten Gesamtschnitt jederzeit aktuell sehen | ich weiß, ob ich meinen angestrebten Abschluss-Schnitt erreiche. |
| S-07 | Student | meine kompletten Studierenden-Informationen inkl. Notendurchschnitt und Anzahl bestandener Module sehen | ich einen vollständigen Überblick über meinen Studienfortschritt habe. |
| S-08 | Student | Details zu einem bestimmten Modul abrufen | ich Informationen wie ECTS, Gewichtung und Beschreibung einsehen kann. |
| S-09 | Student | meine eingeschriebenen Studiengänge einsehen | ich weiß, in welchen Studiengängen ich immatrikuliert bin. |
| S-10 | Student | meine Notenhistorie exportieren | ich meine Leistungen für externe Zwecke dokumentieren kann. |

---

## 3. Lehrenden-Funktionen (Dozent)

| ID | Als... | möchte ich... | damit/weil/denn... |
|----|--------|---------------|-------------------|
| L-01 | Lehrende/r | eine Liste aller Studierenden der Module sehen, die mir zugeordnet sind | ich weiß, für wen ich Noten eintragen muss. |
| L-02 | Lehrende/r | alle meine Noten abrufen | ich eine Übersicht über alle von mir vergebenen Noten habe. |
| L-03 | Lehrende/r | Noten für meine zugewiesenen Module eintragen | die Studierenden bewertet werden können. |
| L-04 | Lehrende/r | Notenlisten bearbeiten und zur Validierung markieren | das Prüfungsamt die Noten prüfen und freigeben kann. |
| L-05 | Lehrende/r | Noten für Modulteilnehmer vorläufig speichern | die Leistungen zunächst im System erfasst sind, bevor die finale Validierung erfolgt. |
| L-06 | Lehrende/r | Noten final validieren | diese für Studierende sichtbar werden und in die Durchschnittsberechnung einfließen. |
| L-07 | Lehrende/r | meine zugewiesenen Module einsehen | ich weiß, für welche Module ich verantwortlich bin. |
| L-08 | Lehrende/r | Noten nach Status filtern (vorläufig/zur Validierung/validiert) | ich den Überblick über den Bearbeitungsstand behalte. |

---

## 4. Prüfungsamt-Funktionen

### 4.1 Notenverwaltung

| ID | Als... | möchte ich... | damit/weil/denn... |
|----|--------|---------------|-------------------|
| P-01 | Prüfungsamt | alle Noten im System sehen | ich einen vollständigen Überblick über alle Bewertungen habe. |
| P-02 | Prüfungsamt | Noten nach Status filtern | ich gezielt z. B. alle validierten oder alle offenen Noten sehen kann. |
| P-03 | Prüfungsamt | Noten final validieren | diese für Studierende sichtbar werden und in die Durchschnittsberechnung einfließen. |
| P-04 | Prüfungsamt | zu validierende Noten mit verschiedenen Filtern suchen | ich effizient nach Modul, Student, Lehrender, Studiengang oder Semester filtern kann. |
| P-05 | Prüfungsamt | Notenlisten validieren und freigeben | Studierende ihre endgültigen Noten erhalten. |
| P-06 | Prüfungsamt | Noten auch nach Freigabe ändern | spätere Korrekturen möglich bleiben. |
| P-07 | Prüfungsamt | Noten löschen | fehlerhafte oder ungültige Einträge entfernt werden können. |
| P-08 | Prüfungsamt | Notenänderungen mit Begründung dokumentieren | Änderungen nachvollziehbar sind. |

### 4.2 Benutzer- und Studiengangsverwaltung

| ID | Als... | möchte ich... | damit/weil/denn... |
|----|--------|---------------|-------------------|
| P-09 | Prüfungsamt | Studierende anlegen und ihnen Studiengänge zuordnen | neue Studierende korrekt registriert werden. |
| P-10 | Prüfungsamt | Lehrende anlegen und ihnen Module zuordnen | Lehrende korrekt ihre Studierendenlisten erhalten. |
| P-11 | Prüfungsamt | Studierende und Lehrende Modulen zuordnen | der gesamte Bewertungsworkflow korrekt funktioniert. |
| P-12 | Prüfungsamt | Studierende mit Stammdaten und Studiengang neu anlegen | diese Zugriff auf das Portal und Prüfungsleistungen erhalten können. |
| P-13 | Prüfungsamt | Nutzer im eigenen Fachbereich aktivieren | deaktivierte Accounts wieder freigeschaltet werden können. |
| P-14 | Prüfungsamt | Nutzerdaten bearbeiten | Stammdaten aktuell und korrekt sind. |
| P-15 | Prüfungsamt | Studierende aus Modulen entfernen | Abmeldungen oder Exmatrikulationen korrekt abgebildet werden. |
| P-16 | Prüfungsamt | Nutzer im eigenen Fachbereich deaktivieren | Accounts temporär gesperrt werden können. |

### 4.3 Modulverwaltung

| ID | Als... | möchte ich... | damit/weil/denn... |
|----|--------|---------------|-------------------|
| P-17 | Prüfungsamt | neue Module anlegen | neue Lehrveranstaltungen im System verfügbar sind. |
| P-18 | Prüfungsamt | Module aktualisieren | Änderungen an Modulnamen, ECTS oder Gewichtung vorgenommen werden können. |
| P-19 | Prüfungsamt | Module löschen | nicht mehr benötigte Module entfernt werden können. |
| P-20 | Prüfungsamt | Module aktivieren/deaktivieren | Module temporär aus der Berechnung ausgeschlossen werden können, ohne sie zu löschen. |
| P-21 | Prüfungsamt | nur aktive Module einer Prüfungsordnung abrufen | ich für Dropdowns/Formulare nur relevante Module anzeigen kann. |
| P-22 | Prüfungsamt | die Gewichtungen von Modulnoten gemäß PO pro Studiengang anpassen | die Gesamtnoten korrekt und regelkonform berechnet werden. |

### 4.4 Prüfungsordnungsverwaltung

| ID | Als... | möchte ich... | damit/weil/denn... |
|----|--------|---------------|-------------------|
| P-23 | Prüfungsamt | Prüfungsordnungen anlegen | neue Regelwerke für Studiengänge definiert werden können. |
| P-24 | Prüfungsamt | Prüfungsordnungen einsehen | ich weiß, welche POs aktiv sind. |
| P-25 | Prüfungsamt | Prüfungsordnungen aktualisieren | Änderungen an bestehenden POs vorgenommen werden können. |
| P-26 | Prüfungsamt | Prüfungsordnungen aktivieren/deaktivieren | alte POs ausgeblendet und neue aktiviert werden können. |
| P-27 | Prüfungsamt | Module einer Prüfungsordnung zuordnen | die Gewichtungen pro Studiengang korrekt berechnet werden. |

---

## 5. Administrator-Funktionen

### 5.1 Mandanten-/Fachbereichsverwaltung

| ID | Als... | möchte ich... | damit/weil/denn... |
|----|--------|---------------|-------------------|
| A-01 | Administrator | Fachbereiche anlegen | neue organisatorische Einheiten im System verfügbar sind. |
| A-02 | Administrator | Fachbereiche einsehen und auflisten | ich einen Überblick über die Mandantenstruktur habe. |
| A-03 | Administrator | Fachbereiche aktualisieren | Name, Beschreibung und Status angepasst werden können. |
| A-04 | Administrator | Fachbereiche aktivieren/deaktivieren | Fachbereiche temporär gesperrt werden können. |
| A-05 | Administrator | Fachbereiche löschen | nicht mehr benötigte Mandanten entfernt werden können. |
| A-06 | Administrator | Studiengänge zu Fachbereichen hinzufügen | neue Studiengänge einem Fachbereich zugeordnet werden können. |
| A-07 | Administrator | Studiengänge von Fachbereichen entfernen | die Zuordnung bereinigt werden kann. |

### 5.2 Benutzerverwaltung

| ID | Als... | möchte ich... | damit/weil/denn... |
|----|--------|---------------|-------------------|
| A-08 | Administrator | Nutzer dauerhaft löschen | DSGVO-konforme Löschungen möglich sind. |
| A-09 | Administrator | neue Prüfungsamtsnutzer erstellen | der Zugriff auf diese sensible Rolle kontrolliert bleibt. |
| A-10 | Administrator | alle Nutzer verwalten | der Systembetrieb technisch und organisatorisch gesichert bleibt. |
| A-11 | Administrator | Nutzerrollen ändern | Berechtigungen bei Bedarf angepasst werden können. |
| A-12 | Administrator | Nutzer systemweit suchen und filtern | ich schnell bestimmte Benutzer finden kann. |

### 5.3 Systemverwaltung

| ID | Als... | möchte ich... | damit/weil/denn... |
|----|--------|---------------|-------------------|
| A-13 | Administrator | den Systemstatus/Health einsehen | ich den Betriebszustand aller Services überwachen kann. |
| A-14 | Administrator | Audit-Logs einsehen | Änderungen im System nachvollziehbar sind. |
| A-15 | Administrator | Systemkonfigurationen anpassen | Einstellungen zentral verwaltet werden können. |
| A-16 | Administrator | Fehlerprotokolle einsehen | Probleme schnell identifiziert werden können. |

---

## Noten-Status Workflow

Die Noten durchlaufen folgende Status:

```
┌─────────┐      ┌──────────────┐      ┌───────────┐
│   ADD   │ ──▶  │ TO_VALIDATE  │ ──▶  │ PUBLISHED │
│(erfasst)│      │(zur Prüfung) │      │(freigeben)│
└─────────┘      └──────────────┘      └───────────┘
     │                  │                    │
     │                  │                    │
   Dozent/          Dozent             Prüfungsamt
  Prüfungsamt    (noteSetzen)       (notenValidieren)
```

| Status | Bedeutung | Sichtbar für Student |
|--------|-----------|---------------------|
| **ADD** | Note erfasst, noch kein Wert | Nein |
| **TO_VALIDATE** | Note eingetragen, wartet auf Validierung | Nein (vorläufig) |
| **PUBLISHED** | Note validiert und freigegeben | Ja |

---

## Zusammenfassung

| Rolle | Anzahl User Stories |
|-------|-------------------|
| Nutzer | 6 |
| Student | 10 |
| Lehrende/r | 8 |
| Prüfungsamt | 27 |
| Administrator | 16 |
| **Gesamt** | **67** |



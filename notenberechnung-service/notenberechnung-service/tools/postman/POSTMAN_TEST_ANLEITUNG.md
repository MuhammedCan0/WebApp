# Postman Test-Anleitung: Notenberechnungsservice

## Voraussetzungen
- Notenberechnungsservice läuft (z.B. http://localhost:8083)
- User-Tenant-Auth-Service läuft (z.B. http://localhost:8085)
- Postman Collection importiert (`notenberechnung-service.postman_collection.json`)

---

## 1. Authentifizierung (JWT-Tokens holen)
1. Öffne in Postman den Ordner **0. Setup - Authentication**
2. Führe nacheinander die Requests aus:
   - **Login as Admin**
   - **Login as Prüfungsamt**
   - **Login as Lehrender**
   - **Login as Student**
3. Die Tokens werden automatisch in den Collection-Variablen gespeichert.

---

## 2. Module anzeigen (Prüfungsamt)
1. Öffne **1. Module Management (Prüfungsamt)**
2. Führe z.B. **List All Modules (Accounting_and_Finance_MA)** aus.
   - Prüfe, ob alle Module korrekt angezeigt werden.

---

## 3. Notenberechnung testen (Student)
1. Öffne **2. Grade Calculation**
2. Wähle z.B. **Calculate Grades - Accounting_and_Finance_MA Full Example**
3. Passe ggf. die Modul-IDs im Body an (z.B. 6 und 7).
4. Führe den Request aus.
   - Prüfe, ob eine Gesamtnote berechnet wird.

---

## 4. Fehlerfälle testen
1. Öffne **5. Error Cases**
2. Führe z.B. **Calculate - Empty Grades List** aus.
   - Prüfe, ob ein Fehler (HTTP 400) zurückkommt.

---

## 5. Multi-Tenant und Rollen-Tests
1. Öffne **6. Role-Based Access Tests** und **7. Multi-Tenant Tests**
2. Führe die jeweiligen Requests aus, um Berechtigungen und Tenant-Funktionalität zu prüfen.

---

## Hinweise
- Nach jedem Service-Neustart müssen die Logins erneut ausgeführt werden!
- Bei 401-Fehler: Token abgelaufen → Logins erneut ausführen.

---

**Viel Erfolg beim Testen!**
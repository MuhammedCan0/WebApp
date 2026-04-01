# Noten- und Modulverwaltung Service

GraphQL-Microservice zur Verwaltung von Noten, Modulen und Studentendaten.

## Features
- GraphQL API für Noten- und Modulverwaltung
- H2 In-Memory Datenbank (Entwicklung)
- JPA/Hibernate Persistenz
- Validierung von Eingaben
- GraphiQL UI zum Testen der Queries/Mutations

## Technik-Stack
- Java 17
- Spring Boot 3.5.x
- Spring GraphQL
- Spring Data JPA
- H2 (Dev) / PostgreSQL (Runtime)

## Konfiguration
Die wichtigsten Einstellungen befinden sich in [src/main/resources/application.yaml](src/main/resources/application.yaml).

- Server-Port: `8082`
- GraphQL Endpoint: `/graphql`
- GraphiQL UI: `/graphiql`
- H2 Console: `/h2-console`
- JWT Secret: `jwt.secret` (muss mit Auth-Service/Gateway übereinstimmen)

## Lokaler Start

### Voraussetzungen
- Java 17
- Maven Wrapper (liegt im Projekt)

### Starten
- Windows:
  - `mvnw.cmd spring-boot:run`
- macOS/Linux:
  - `./mvnw spring-boot:run`

Service läuft anschließend unter:
- GraphQL: http://localhost:8082/graphql
- GraphiQL: http://localhost:8082/graphiql

## GraphQL Schema
Das Schema liegt in [src/main/resources/graphql/schema.graphqls](src/main/resources/graphql/schema.graphqls).

### Beispiel Queries

```graphql
query {
  alleNoten {
    id
    matrikelnummer
    modulId
    note
    status
  }
}
```

```graphql
mutation {
  modulErstellen(input: {
    modulId: 101,
    tenantId: "tenant-1",
    modulName: "Web Engineering",
    ects: 6,
    gewichtung: 1.0,
    studiengang: "INF",
    semester: 3,
    beschreibung: "Grundlagen Web-Entwicklung"
  }) {
    id
    modulId
    modulName
  }
}
```

## Tests
Tests ausführen:
- Windows: `mvnw.cmd test`
- macOS/Linux: `./mvnw test`

## Build
Jar bauen:
- Windows: `mvnw.cmd clean package`
- macOS/Linux: `./mvnw clean package`

## Hinweise
- Für Produktion ist PostgreSQL vorgesehen (Abhängigkeit ist vorhanden).
- JWT-Konfiguration muss mit dem Auth-Service und Gateway abgestimmt sein.

package com.university.notenberechnung_service.dto;

import lombok.Data;

@Data
public class GraphQLNote {
    private String id;
    private String matrikelnummer;
    private String tenantId;
    private String modulId;
    private String modulName;
    private String lehrendenMatrikelnummer;
    private Double note;
    private String status;
    private String studiengang;
    private String semester;
    private String erstelltAm;
    private String aktualisiertAm;
}
package com.university.notenberechnung_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotenVonStudentResponse {
    private List<GraphQLNote> notenVonStudent;
}
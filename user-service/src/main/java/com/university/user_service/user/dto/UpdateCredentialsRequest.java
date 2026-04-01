package com.university.user_service.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCredentialsRequest {

    private String email;
    private String password;
    private Long matrikelnummer;
}

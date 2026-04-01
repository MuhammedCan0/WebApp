package com.university.auth_service.auth.credentials;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthCredentials {

    private Long matrikelnummer;
    private String email;
    private String passwordHash;
}

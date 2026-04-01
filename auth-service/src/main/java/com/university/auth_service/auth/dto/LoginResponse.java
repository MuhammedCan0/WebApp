package com.university.auth_service.auth.dto;

import com.university.auth_service.common.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private UUID userId;
    private String email;
    private String token;
    private Set<Role> roles;
    private long expiresIn;
}

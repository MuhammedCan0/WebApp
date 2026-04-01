package com.university.auth_service.auth;

import com.university.auth_service.user.client.UserAuthDto;

record AuthResult(String token, String email, UserAuthDto user) {
}

package com.university.tenant_service.common;

public enum Role {
    ADMIN("Full access to tenant management"),
    PRUEFUNGSAMT("User management without tenant admin rights"),
    LEHRENDER("No tenant admin rights"),
    STUDENT("No tenant admin rights");

    Role(String description) {
    }
}

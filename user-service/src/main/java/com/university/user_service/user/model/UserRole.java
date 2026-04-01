package com.university.user_service.user.model;

import com.university.user_service.common.BaseEntity;
import com.university.user_service.common.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserRole extends BaseEntity {

    private java.util.UUID userId;
    private Role role;
}

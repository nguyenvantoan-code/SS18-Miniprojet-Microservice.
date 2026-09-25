package com.rikkeibank.identity.dto;

import com.rikkeibank.identity.entity.Role;
import com.rikkeibank.identity.entity.User;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Role   role;
    private boolean enabled;
    private LocalDateTime createdAt;

    public static UserResponse from(User u) {
        return UserResponse.builder()
            .id(u.getId()).username(u.getUsername())
            .email(u.getEmail()).fullName(u.getFullName())
            .role(u.getRole()).enabled(u.isEnabled())
            .createdAt(u.getCreatedAt()).build();
    }
}

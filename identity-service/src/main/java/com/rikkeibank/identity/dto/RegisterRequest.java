package com.rikkeibank.identity.dto;

import com.rikkeibank.identity.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(min=3, max=50) private String username;
    @NotBlank @Size(min=8)         private String password;
    @NotBlank @Email               private String email;
    @NotBlank                      private String fullName;
    private Role role = Role.CUSTOMER;
}

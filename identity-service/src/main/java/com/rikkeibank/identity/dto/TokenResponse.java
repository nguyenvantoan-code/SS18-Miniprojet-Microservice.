package com.rikkeibank.identity.dto;

import lombok.*;

@Data @Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long   expiresIn;
    private String role;
}

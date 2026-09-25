package com.rikkeibank.identity.service;

import com.rikkeibank.identity.dto.*;
import com.rikkeibank.identity.entity.*;
import com.rikkeibank.identity.repository.*;
import com.rikkeibank.identity.util.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepo;
    @Mock RefreshTokenRepository rtRepo;
    @Mock PasswordEncoder encoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    @Test
    void login_success() {
        User user = User.builder().id(1L).username("teller1")
            .password("hashed").role(Role.TELLER).enabled(true).build();
        when(userRepo.findByUsername("teller1")).thenReturn(Optional.of(user));
        when(encoder.matches("pass123", "hashed")).thenReturn(true);
        when(jwtUtil.generateAccessToken("teller1","TELLER")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("teller1")).thenReturn("refresh-token");

        TokenResponse resp = authService.login(new LoginRequest() {{
            setUsername("teller1"); setPassword("pass123");
        }});

        assertThat(resp.getAccessToken()).isEqualTo("access-token");
        verify(rtRepo, times(1)).save(any());
    }

    @Test
    void login_badCredentials_throwsException() {
        User user = User.builder().id(1L).username("user1")
            .password("hashed").role(Role.CUSTOMER).enabled(true).build();
        when(userRepo.findByUsername("user1")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest() {{
            setUsername("user1"); setPassword("wrong");
        }})).isInstanceOf(BadCredentialsException.class);
    }
}

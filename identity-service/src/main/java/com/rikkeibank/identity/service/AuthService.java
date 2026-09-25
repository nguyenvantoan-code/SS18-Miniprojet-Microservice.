package com.rikkeibank.identity.service;

import com.rikkeibank.identity.dto.*;
import com.rikkeibank.identity.entity.*;
import com.rikkeibank.identity.repository.*;
import com.rikkeibank.identity.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository rtRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("Username đã tồn tại: " + req.getUsername());
        if (userRepo.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email đã được sử dụng");

        User user = User.builder()
            .username(req.getUsername())
            .password(passwordEncoder.encode(req.getPassword()))
            .email(req.getEmail())
            .fullName(req.getFullName())
            .role(req.getRole() != null ? req.getRole() : Role.CUSTOMER)
            .build();

        log.info("Đăng ký người dùng mới: {}", req.getUsername());
        return UserResponse.from(userRepo.save(user));
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        User user = userRepo.findByUsername(req.getUsername())
            .orElseThrow(() -> new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu"));

        if (!user.isEnabled())
            throw new DisabledException("Tài khoản bị khóa");

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu");

        String accessToken  = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        rtRepo.deleteByUserId(user.getId());
        rtRepo.save(RefreshToken.builder()
            .token(refreshToken)
            .userId(user.getId())
            .expiresAt(Instant.now().plusMillis(604800000L))
            .build());

        log.info("Đăng nhập thành công: {}", user.getUsername());
        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(900)
            .role(user.getRole().name())
            .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        rtRepo.findByToken(refreshToken).ifPresent(rt -> {
            rt.setRevoked(true);
            rtRepo.save(rt);
            log.info("Đã logout - token đã bị thu hồi");
        });
    }

    /** ADMIN: Thu hồi quyền truy cập ngay lập tức */
    @Transactional
    public void revokeAllTokens(Long userId) {
        rtRepo.deleteByUserId(userId);
        log.info("ADMIN thu hồi toàn bộ token của userId={}", userId);
    }
}

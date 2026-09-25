package com.rikkeibank.identity.service;

import com.rikkeibank.identity.dto.UserResponse;
import com.rikkeibank.identity.entity.User;
import com.rikkeibank.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    public List<UserResponse> getAllUsers() {
        return userRepo.findAll().stream().map(UserResponse::from).toList();
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {
        log.info("Querying DB for user id={}", id);
        User u = userRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user id=" + id));
        return UserResponse.from(u);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void deleteUser(Long id) {
        userRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user id=" + id));
        userRepo.deleteById(id);
        log.info("Đã xóa user id={}", id);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public UserResponse toggleEnabled(Long id, boolean enabled) {
        User u = userRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user id=" + id));
        u.setEnabled(enabled);
        log.info("{} user id={}", enabled ? "Kích hoạt" : "Vô hiệu hóa", id);
        return UserResponse.from(userRepo.save(u));
    }
}

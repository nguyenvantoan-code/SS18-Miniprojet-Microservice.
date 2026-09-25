package com.rikkeibank.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
            "service", "notification-service",
            "status", "UP",
            "consumer", "Reactive Kafka Consumer đang hoạt động"
        )));
    }
}

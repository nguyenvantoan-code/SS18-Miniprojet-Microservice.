package com.rikkeibank.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * FeignClient gọi customer-service để xác minh customer tồn tại.
 * Spring Cloud LoadBalancer tự động phân phối giữa các instance.
 * Circuit Breaker: CLOSED → OPEN → HALF-OPEN khi customer-service gặp sự cố.
 */
@FeignClient(name = "customer-service", fallback = CustomerClientFallback.class)
public interface CustomerClient {
    @GetMapping("/api/customers/{id}")
    JsonNode getCustomerById(@PathVariable Long id);
}

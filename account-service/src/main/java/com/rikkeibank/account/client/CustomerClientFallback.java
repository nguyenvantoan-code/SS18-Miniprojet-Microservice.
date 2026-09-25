package com.rikkeibank.account.client;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j @Component
public class CustomerClientFallback implements CustomerClient {
    @Override
    public JsonNode getCustomerById(Long id) {
        log.error("Circuit Breaker OPEN: customer-service không khả dụng cho customerId={}", id);
        return null; // caller phải kiểm tra null
    }
}

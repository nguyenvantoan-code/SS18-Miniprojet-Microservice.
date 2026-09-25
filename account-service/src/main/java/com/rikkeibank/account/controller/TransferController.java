package com.rikkeibank.account.controller;

import com.rikkeibank.account.dto.TransferRequest;
import com.rikkeibank.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @RequestMapping("/api/transfers") @RequiredArgsConstructor
public class TransferController {

    private final AccountService svc;

    @PostMapping
    public ResponseEntity<Map<String,Object>> transfer(
            @Valid @RequestBody TransferRequest req,
            @RequestHeader(value="X-User-Id", required=false, defaultValue="system") String userId) {
        String transferId = svc.initiateTransfer(req, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
            "transferId", transferId,
            "status", "PROCESSING",
            "message", "Chuyển khoản đang được xử lý qua Saga"
        ));
    }
}

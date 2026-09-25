package com.rikkeibank.transaction.controller;
import com.rikkeibank.transaction.dto.TransactionResponse;
import com.rikkeibank.transaction.entity.Transaction;
import com.rikkeibank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.List;

@RestController @RequestMapping("/api/transactions") @RequiredArgsConstructor
public class TransactionController {
    private final TransactionRepository txRepo;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll() {
        return ResponseEntity.ok(txRepo.findAll().stream().map(TransactionResponse::from).toList());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(txRepo.findBySourceAccountIdOrTargetAccountId(accountId, accountId)
            .stream().map(TransactionResponse::from).toList());
    }

    @GetMapping("/today")
    public ResponseEntity<List<TransactionResponse>> getToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = start.plusDays(1).minusNanos(1);
        return ResponseEntity.ok(txRepo.findByCreatedAtBetween(start, end)
            .stream().map(TransactionResponse::from).toList());
    }
}

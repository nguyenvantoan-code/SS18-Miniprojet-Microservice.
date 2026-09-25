package com.rikkeibank.account.controller;

import com.rikkeibank.account.dto.*;
import com.rikkeibank.account.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/accounts") @RequiredArgsConstructor
public class AccountController {

    private final AccountService svc;

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(svc.createAccount(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(svc.getById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(svc.getByCustomerId(customerId));
    }

    @PutMapping("/{id}/freeze")
    public ResponseEntity<AccountResponse> freeze(@PathVariable Long id) {
        return ResponseEntity.ok(svc.freeze(id));
    }
}

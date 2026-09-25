package com.rikkeibank.customer.controller;
import com.rikkeibank.customer.dto.*;
import com.rikkeibank.customer.service.AccountTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/account-types") @RequiredArgsConstructor
public class AccountTypeController {
    private final AccountTypeService svc;

    @GetMapping public ResponseEntity<List<AccountTypeResponse>> getAll() { return ResponseEntity.ok(svc.getAll()); }
    @GetMapping("/{id}") public ResponseEntity<AccountTypeResponse> getById(@PathVariable Long id) { return ResponseEntity.ok(svc.getById(id)); }
    @PostMapping public ResponseEntity<AccountTypeResponse> create(@Valid @RequestBody AccountTypeRequest req) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(req)); }
    @PutMapping("/{id}") public ResponseEntity<AccountTypeResponse> update(@PathVariable Long id, @Valid @RequestBody AccountTypeRequest req) { return ResponseEntity.ok(svc.update(id, req)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { svc.delete(id); return ResponseEntity.noContent().build(); }
}

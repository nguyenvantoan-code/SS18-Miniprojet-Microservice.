package com.rikkeibank.customer.controller;
import com.rikkeibank.customer.dto.*;
import com.rikkeibank.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/customers") @RequiredArgsConstructor
public class CustomerController {
    private final CustomerService svc;

    @GetMapping public ResponseEntity<List<CustomerResponse>> getAll() { return ResponseEntity.ok(svc.getAll()); }
    @GetMapping("/{id}") public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) { return ResponseEntity.ok(svc.getById(id)); }
    @PostMapping public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest req) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(req)); }
    @PutMapping("/{id}") public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest req) { return ResponseEntity.ok(svc.update(id, req)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { svc.delete(id); return ResponseEntity.noContent().build(); }
}

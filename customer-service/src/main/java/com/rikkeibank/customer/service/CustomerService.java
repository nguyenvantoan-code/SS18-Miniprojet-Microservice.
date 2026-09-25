package com.rikkeibank.customer.service;
import com.rikkeibank.customer.dto.*;
import com.rikkeibank.customer.entity.Customer;
import com.rikkeibank.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j @Service @RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repo;

    public List<CustomerResponse> getAll() {
        return repo.findAll().stream().map(CustomerResponse::from).toList();
    }

    @Cacheable(value = "customers", key = "#id")
    public CustomerResponse getById(Long id) {
        log.info("Querying DB for customer id={}", id);
        return CustomerResponse.from(repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy customer id=" + id)));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest req) {
        if (repo.findByCitizenId(req.getCitizenId()).isPresent())
            throw new IllegalArgumentException("Số CMND/CCCD đã tồn tại");
        Customer c = Customer.builder()
            .citizenId(req.getCitizenId()).fullName(req.getFullName())
            .email(req.getEmail()).phone(req.getPhone())
            .dateOfBirth(req.getDateOfBirth()).address(req.getAddress())
            .userId(req.getUserId()).build();
        log.info("Tạo mới customer: {}", req.getFullName());
        return CustomerResponse.from(repo.save(c));
    }

    @Transactional @CachePut(value = "customers", key = "#id")
    public CustomerResponse update(Long id, CustomerRequest req) {
        Customer c = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy customer id=" + id));
        c.setFullName(req.getFullName()); c.setEmail(req.getEmail());
        c.setPhone(req.getPhone()); c.setAddress(req.getAddress());
        log.info("Cập nhật customer id={}", id);
        return CustomerResponse.from(repo.save(c));
    }

    @Transactional @CacheEvict(value = "customers", key = "#id")
    public void delete(Long id) {
        repo.deleteById(id);
        log.info("Xóa customer id={}", id);
    }
}

package com.rikkeibank.customer.service;
import com.rikkeibank.customer.dto.*;
import com.rikkeibank.customer.entity.AccountType;
import com.rikkeibank.customer.repository.AccountTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j @Service @RequiredArgsConstructor
public class AccountTypeService {
    private final AccountTypeRepository repo;

    public List<AccountTypeResponse> getAll() {
        return repo.findAll().stream().map(AccountTypeResponse::from).toList();
    }

    @Cacheable(value = "accountTypes", key = "#id")
    public AccountTypeResponse getById(Long id) {
        log.info("Querying DB for accountType id={}", id);
        return AccountTypeResponse.from(repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy account type id=" + id)));
    }

    @Transactional
    public AccountTypeResponse create(AccountTypeRequest req) {
        AccountType at = AccountType.builder()
            .name(req.getName()).description(req.getDescription())
            .interestRate(req.getInterestRate()).minimumBalance(req.getMinimumBalance()).build();
        return AccountTypeResponse.from(repo.save(at));
    }

    @Transactional @CachePut(value = "accountTypes", key = "#id")
    public AccountTypeResponse update(Long id, AccountTypeRequest req) {
        AccountType at = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy account type id=" + id));
        at.setName(req.getName()); at.setDescription(req.getDescription());
        at.setInterestRate(req.getInterestRate()); at.setMinimumBalance(req.getMinimumBalance());
        return AccountTypeResponse.from(repo.save(at));
    }

    @Transactional @CacheEvict(value = "accountTypes", key = "#id")
    public void delete(Long id) {
        repo.deleteById(id);
    }
}

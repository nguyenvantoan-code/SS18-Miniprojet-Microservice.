package com.rikkeibank.account.service;
import com.rikkeibank.account.client.CustomerClient;
import com.rikkeibank.account.dto.*;
import com.rikkeibank.account.entity.*;
import com.rikkeibank.account.event.*;
import com.rikkeibank.account.repository.BankAccountRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j @Service @RequiredArgsConstructor
public class AccountService {

    private final BankAccountRepository accountRepo;
    private final CustomerClient customerClient;
    private final KafkaTemplate<String, TransferEvent> kafkaTemplate;

    /** Tạo tài khoản - xác minh customer qua FeignClient + Circuit Breaker */
    @Transactional
    @CircuitBreaker(name = "customerService", fallbackMethod = "createAccountFallback")
    public AccountResponse createAccount(AccountRequest req) {
        var customer = customerClient.getCustomerById(req.getCustomerId());
        if (customer == null) throw new RuntimeException("Không xác minh được customer, thử lại sau");

        String accountNumber = generateAccountNumber();
        BigDecimal initial = req.getInitialDeposit() != null ? req.getInitialDeposit() : BigDecimal.ZERO;

        BankAccount acct = BankAccount.builder()
            .accountNumber(accountNumber)
            .customerId(req.getCustomerId())
            .accountTypeId(req.getAccountTypeId())
            .balance(initial)
            .build();
        log.info("Tạo tài khoản {} cho customerId={}", accountNumber, req.getCustomerId());
        return AccountResponse.from(accountRepo.save(acct));
    }

    public AccountResponse createAccountFallback(AccountRequest req, Throwable t) {
        log.error("Circuit Breaker: customer-service không khả dụng - {}", t.getMessage());
        throw new RuntimeException("Không thể tạo tài khoản do dịch vụ khách hàng tạm thời không khả dụng");
    }

    @Cacheable(value = "accounts", key = "#id")
    public AccountResponse getById(Long id) {
        log.info("Querying DB for account id={}", id);
        return AccountResponse.from(findAccount(id));
    }

    public List<AccountResponse> getByCustomerId(Long customerId) {
        return accountRepo.findByCustomerId(customerId).stream().map(AccountResponse::from).toList();
    }

    @CacheEvict(value = "accounts", key = "#id")
    @Transactional
    public AccountResponse freeze(Long id) {
        BankAccount acct = findAccount(id);
        acct.setStatus(AccountStatus.FROZEN);
        log.info("Tài khoản id={} đã bị đóng băng", id);
        return AccountResponse.from(accountRepo.save(acct));
    }

    /**
     * Câu 3 - Saga Choreography: Bắt đầu Saga chuyển khoản.
     * Publish sự kiện TRANSFER_INITIATED lên Kafka.
     * Các service khác lắng nghe và phản hồi.
     */
    @Transactional
    public String initiateTransfer(TransferRequest req, String initiatedBy) {
        BankAccount source = findAccount(req.getSourceAccountId());

        if (source.getStatus() != AccountStatus.ACTIVE)
            throw new IllegalStateException("Tài khoản nguồn không ở trạng thái ACTIVE");
        if (source.getBalance().compareTo(req.getAmount()) < 0)
            throw new IllegalStateException("Số dư không đủ để chuyển khoản");

        // Confirm target exists
        findAccount(req.getTargetAccountId());

        String transferId = UUID.randomUUID().toString();

        // Trừ tiền tài khoản nguồn NGAY (bước 1 của Saga)
        source.setBalance(source.getBalance().subtract(req.getAmount()));
        accountRepo.save(source);
        log.info("Saga DEBIT: Đã trừ {} từ tài khoản {} (transferId={})",
            req.getAmount(), source.getAccountNumber(), transferId);

        // Publish DEBIT_COMPLETED để tiếp tục Saga
        TransferEvent event = TransferEvent.builder()
            .transferId(transferId)
            .sourceAccountId(req.getSourceAccountId())
            .targetAccountId(req.getTargetAccountId())
            .amount(req.getAmount())
            .description(req.getDescription())
            .initiatedBy(initiatedBy)
            .type(TransferEventType.DEBIT_COMPLETED)
            .build();
        kafkaTemplate.send(BankingTopics.TRANSFER, transferId, event);
        log.info("Đã gửi sự kiện DEBIT_COMPLETED lên Kafka (transferId={})", transferId);

        return transferId;
    }

    /** Được gọi bởi Kafka consumer khi nhận CREDIT event */
    @Transactional
    public void creditAccount(Long accountId, BigDecimal amount, String transferId) {
        BankAccount target = findAccount(accountId);
        if (target.getStatus() != AccountStatus.ACTIVE)
            throw new IllegalStateException("Tài khoản đích không ACTIVE");

        target.setBalance(target.getBalance().add(amount));
        accountRepo.save(target);
        log.info("Saga CREDIT: Cộng {} vào tài khoản {} (transferId={})",
            amount, target.getAccountNumber(), transferId);
    }

    /** Compensate: hoàn tiền về tài khoản nguồn khi CREDIT thất bại */
    @Transactional
    public void compensateDebit(Long sourceAccountId, BigDecimal amount, String transferId) {
        BankAccount source = findAccount(sourceAccountId);
        source.setBalance(source.getBalance().add(amount));
        accountRepo.save(source);
        log.error("Saga COMPENSATE: Hoàn {} về tài khoản {} (transferId={})",
            amount, source.getAccountNumber(), transferId);
    }

    private BankAccount findAccount(Long id) {
        return accountRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản id=" + id));
    }

    private String generateAccountNumber() {
        return "RKK" + String.format("%010d", System.currentTimeMillis() % 10_000_000_000L);
    }
}

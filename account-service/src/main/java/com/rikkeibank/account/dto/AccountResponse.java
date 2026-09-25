package com.rikkeibank.account.dto;
import com.rikkeibank.account.entity.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private Long customerId;
    private Long accountTypeId;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime createdAt;

    public static AccountResponse from(BankAccount a) {
        return AccountResponse.builder()
            .id(a.getId()).accountNumber(a.getAccountNumber())
            .customerId(a.getCustomerId()).accountTypeId(a.getAccountTypeId())
            .balance(a.getBalance()).status(a.getStatus())
            .createdAt(a.getCreatedAt()).build();
    }
}

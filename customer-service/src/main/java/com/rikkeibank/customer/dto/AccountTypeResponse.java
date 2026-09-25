package com.rikkeibank.customer.dto;
import com.rikkeibank.customer.entity.AccountType;
import lombok.*;
import java.math.BigDecimal;
@Data @Builder
public class AccountTypeResponse {
    private Long id;
    private String name, description;
    private BigDecimal interestRate, minimumBalance;
    public static AccountTypeResponse from(AccountType at) {
        return AccountTypeResponse.builder()
            .id(at.getId()).name(at.getName()).description(at.getDescription())
            .interestRate(at.getInterestRate()).minimumBalance(at.getMinimumBalance()).build();
    }
}

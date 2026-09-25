package com.rikkeibank.account.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AccountRequest {
    @NotNull private Long customerId;
    @NotNull private Long accountTypeId;
    @DecimalMin("0.0") private java.math.BigDecimal initialDeposit;
}

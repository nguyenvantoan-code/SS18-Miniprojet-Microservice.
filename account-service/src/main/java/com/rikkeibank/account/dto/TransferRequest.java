package com.rikkeibank.account.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull  private Long   sourceAccountId;
    @NotNull  private Long   targetAccountId;
    @NotNull @DecimalMin("1.0") private BigDecimal amount;
    private String description;
}

package com.rikkeibank.customer.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class AccountTypeRequest {
    @NotBlank private String name;
    private String description;
    @NotNull @DecimalMin("0.0") private BigDecimal interestRate;
    @NotNull @DecimalMin("0.0") private BigDecimal minimumBalance;
}

package com.rikkeibank.customer.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
@Data
public class CustomerRequest {
    @NotBlank @Size(min=9,max=12) private String citizenId;
    @NotBlank private String fullName;
    @Email @NotBlank private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private Long userId;
}

package com.rikkeibank.customer.dto;
import com.rikkeibank.customer.entity.Customer;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @Builder
public class CustomerResponse {
    private Long id;
    private String citizenId, fullName, email, phone, address;
    private LocalDate dateOfBirth;
    private Long userId;
    private LocalDateTime createdAt;
    public static CustomerResponse from(Customer c) {
        return CustomerResponse.builder()
            .id(c.getId()).citizenId(c.getCitizenId()).fullName(c.getFullName())
            .email(c.getEmail()).phone(c.getPhone()).address(c.getAddress())
            .dateOfBirth(c.getDateOfBirth()).userId(c.getUserId())
            .createdAt(c.getCreatedAt()).build();
    }
}

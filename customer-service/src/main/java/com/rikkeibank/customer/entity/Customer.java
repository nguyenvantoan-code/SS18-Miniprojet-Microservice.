package com.rikkeibank.customer.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "customers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=20) private String citizenId;
    @Column(nullable=false, length=150) private String fullName;
    @Column(nullable=false, unique=true, length=150) private String email;
    @Column(length=20) private String phone;
    private LocalDate dateOfBirth;
    @Column(length=250) private String address;
    private Long userId;  // link tới identity-service
    private LocalDateTime createdAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}

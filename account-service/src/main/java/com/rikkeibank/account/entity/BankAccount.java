package com.rikkeibank.account.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "bank_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(nullable=false, unique=true, length=20) private String accountNumber;
    @Column(nullable=false) private Long customerId;
    @Column(nullable=false) private Long accountTypeId;

    @Column(nullable=false, precision=18, scale=2)
    @Builder.Default private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20) @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void onUpdate() { updatedAt = LocalDateTime.now(); }
}

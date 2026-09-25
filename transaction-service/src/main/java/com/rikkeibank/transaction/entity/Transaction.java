package com.rikkeibank.transaction.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=50, unique=true) private String transferId;
    @Column(nullable=false) private Long sourceAccountId;
    @Column(nullable=false) private Long targetAccountId;
    @Column(nullable=false, precision=18, scale=2) private BigDecimal amount;
    @Column(length=250) private String description;
    @Column(length=150) private String initiatedBy;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private TransactionStatus status;
    @Column(length=500) private String failureReason;
    private LocalDateTime createdAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}

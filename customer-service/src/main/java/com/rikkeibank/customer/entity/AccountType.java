package com.rikkeibank.customer.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "account_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=100) private String name;
    @Column(length=250) private String description;
    @Column(nullable=false) private BigDecimal interestRate;
    @Column(nullable=false) private BigDecimal minimumBalance;
}

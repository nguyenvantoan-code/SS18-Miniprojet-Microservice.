package com.rikkeibank.transaction.dto;
import com.rikkeibank.transaction.entity.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class TransactionResponse {
    private Long id;
    private String transferId;
    private Long sourceAccountId, targetAccountId;
    private BigDecimal amount;
    private String description, initiatedBy, failureReason;
    private TransactionStatus status;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
            .id(t.getId()).transferId(t.getTransferId())
            .sourceAccountId(t.getSourceAccountId()).targetAccountId(t.getTargetAccountId())
            .amount(t.getAmount()).description(t.getDescription())
            .initiatedBy(t.getInitiatedBy()).failureReason(t.getFailureReason())
            .status(t.getStatus()).createdAt(t.getCreatedAt()).build();
    }
}

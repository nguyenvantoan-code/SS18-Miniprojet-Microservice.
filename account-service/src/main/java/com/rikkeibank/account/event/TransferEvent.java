package com.rikkeibank.account.event;
import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransferEvent {
    private String  transferId;      // UUID duy nhất cho giao dịch
    private Long    sourceAccountId;
    private Long    targetAccountId;
    private BigDecimal amount;
    private String  description;
    private String  initiatedBy;     // username của người thực hiện
    private TransferEventType type;
    private String  failureReason;
}

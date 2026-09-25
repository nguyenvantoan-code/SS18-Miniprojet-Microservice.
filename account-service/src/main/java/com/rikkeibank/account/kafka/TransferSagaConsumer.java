package com.rikkeibank.account.kafka;

import com.rikkeibank.account.event.*;
import com.rikkeibank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer trong account-service.
 * Nhận sự kiện DEBIT_COMPLETED → thực hiện CREDIT → gửi CREDIT_COMPLETED hoặc CREDIT_FAILED.
 * Nếu CREDIT_FAILED → gửi COMPENSATE_CREDIT để rollback.
 */
@Slf4j @Component @RequiredArgsConstructor
public class TransferSagaConsumer {

    private final AccountService accountService;
    private final KafkaTemplate<String, TransferEvent> kafkaTemplate;

    @KafkaListener(topics = BankingTopics.TRANSFER, groupId = "account-credit-group")
    public void handleTransferEvent(TransferEvent event) {
        log.info("Account-service nhận sự kiện: type={}, transferId={}",
            event.getType(), event.getTransferId());

        if (event.getType() == TransferEventType.DEBIT_COMPLETED) {
            performCredit(event);
        } else if (event.getType() == TransferEventType.CREDIT_FAILED) {
            performCompensate(event);
        }
    }

    private void performCredit(TransferEvent event) {
        try {
            accountService.creditAccount(event.getTargetAccountId(), event.getAmount(), event.getTransferId());

            // Thành công → gửi CREDIT_COMPLETED
            TransferEvent ok = TransferEvent.builder()
                .transferId(event.getTransferId())
                .sourceAccountId(event.getSourceAccountId())
                .targetAccountId(event.getTargetAccountId())
                .amount(event.getAmount())
                .description(event.getDescription())
                .initiatedBy(event.getInitiatedBy())
                .type(TransferEventType.CREDIT_COMPLETED)
                .build();
            kafkaTemplate.send(BankingTopics.TRANSFER, event.getTransferId(), ok);
            log.info("SAGA: Gửi CREDIT_COMPLETED (transferId={})", event.getTransferId());

        } catch (Exception e) {
            log.error("SAGA: Credit that bai (transferId={}): {}", event.getTransferId(), e.getMessage());

            // Thất bại → Compensate, gửi CREDIT_FAILED
            TransferEvent fail = TransferEvent.builder()
                .transferId(event.getTransferId())
                .sourceAccountId(event.getSourceAccountId())
                .targetAccountId(event.getTargetAccountId())
                .amount(event.getAmount())
                .initiatedBy(event.getInitiatedBy())
                .type(TransferEventType.CREDIT_FAILED)
                .failureReason(e.getMessage())
                .build();
            kafkaTemplate.send(BankingTopics.TRANSFER, event.getTransferId(), fail);
        }
    }

    private void performCompensate(TransferEvent event) {
        // Rollback: hoàn tiền về tài khoản nguồn
        accountService.compensateDebit(event.getSourceAccountId(), event.getAmount(), event.getTransferId());
        TransferEvent compensated = TransferEvent.builder()
            .transferId(event.getTransferId())
            .sourceAccountId(event.getSourceAccountId())
            .targetAccountId(event.getTargetAccountId())
            .amount(event.getAmount())
            .initiatedBy(event.getInitiatedBy())
            .type(TransferEventType.COMPENSATE_CREDIT)
            .failureReason(event.getFailureReason())
            .build();
        kafkaTemplate.send(BankingTopics.TRANSFER, event.getTransferId(), compensated);
        log.error("SAGA ROLLBACK hoàn tất: (transferId={})", event.getTransferId());
    }
}

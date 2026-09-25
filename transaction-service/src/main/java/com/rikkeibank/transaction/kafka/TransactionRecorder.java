package com.rikkeibank.transaction.kafka;

import com.rikkeibank.transaction.entity.*;
import com.rikkeibank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

/**
 * Lắng nghe Saga events từ account-service.
 * Ghi nhận giao dịch khi Saga hoàn tất (CREDIT_COMPLETED hoặc COMPENSATE_CREDIT).
 * Sau đó publish sự kiện thông báo lên banking-notification topic.
 */
@Slf4j @Component @RequiredArgsConstructor
public class TransactionRecorder {

    private final TransactionRepository txRepo;
    private final KafkaTemplate<String, JsonNode> notificationKafka;

    @KafkaListener(topics = "banking-transfer", groupId = "transaction-recorder-group")
    public void handleTransferEvent(com.fasterxml.jackson.databind.JsonNode event) {
        String type       = event.path("type").asText();
        String transferId = event.path("transferId").asText();

        if ("CREDIT_COMPLETED".equals(type)) {
            // Giao dịch thành công → ghi record
            Transaction tx = Transaction.builder()
                .transferId(transferId)
                .sourceAccountId(event.path("sourceAccountId").asLong())
                .targetAccountId(event.path("targetAccountId").asLong())
                .amount(new BigDecimal(event.path("amount").asText("0")))
                .description(event.path("description").asText())
                .initiatedBy(event.path("initiatedBy").asText())
                .status(TransactionStatus.COMPLETED)
                .build();
            txRepo.save(tx);
            log.info("Đã ghi nhận giao dịch thành công: transferId={}", transferId);
            publishNotification(event, "Chuyển khoản thành công");

        } else if ("COMPENSATE_CREDIT".equals(type)) {
            // Giao dịch đã bị rollback
            Transaction tx = Transaction.builder()
                .transferId(transferId)
                .sourceAccountId(event.path("sourceAccountId").asLong())
                .targetAccountId(event.path("targetAccountId").asLong())
                .amount(new BigDecimal(event.path("amount").asText("0")))
                .description("ROLLBACK: " + event.path("failureReason").asText())
                .initiatedBy(event.path("initiatedBy").asText())
                .status(TransactionStatus.ROLLBACK)
                .failureReason(event.path("failureReason").asText())
                .build();
            txRepo.save(tx);
            log.error("Đã ghi nhận giao dịch ROLLBACK: transferId={}", transferId);
            publishNotification(event, "Chuyển khoản thất bại và đã được hoàn tác");
        }
    }

    private void publishNotification(JsonNode originalEvent, String message) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode notif = mapper.createObjectNode()
                .put("transferId", originalEvent.path("transferId").asText())
                .put("sourceAccountId", originalEvent.path("sourceAccountId").asLong())
                .put("targetAccountId", originalEvent.path("targetAccountId").asLong())
                .put("amount", originalEvent.path("amount").asText())
                .put("initiatedBy", originalEvent.path("initiatedBy").asText())
                .put("message", message);
            notificationKafka.send("banking-notification", notif);
        } catch (Exception e) {
            log.error("Lỗi khi publish notification: {}", e.getMessage());
        }
    }
}

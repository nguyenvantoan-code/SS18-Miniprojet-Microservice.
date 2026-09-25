package com.rikkeibank.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.*;
import jakarta.annotation.PostConstruct;

import java.util.*;

/**
 * Câu bonus: WebFlux + Reactor Kafka - Consumer xử lý thông báo theo kiểu REACTIVE.
 * Minh chứng tư duy Async / Event-driven / Loose Coupling.
 *
 * Luồng: banking-notification topic → flatMap → xử lý bất đồng bộ → log thông báo.
 */
@Slf4j
@Component
public class NotificationConsumer {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void startListening() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-reactive-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        ReceiverOptions<String, String> options = ReceiverOptions.<String, String>create(props)
            .subscription(Collections.singleton("banking-notification"));

        KafkaReceiver.create(options).receive()
            .flatMap(record -> {
                record.receiverOffset().acknowledge();
                return Flux.just(record.value())
                    .map(this::processNotification)
                    .onErrorResume(e -> {
                        log.error("Lỗi xử lý notification: {}", e.getMessage());
                        return Flux.empty();
                    });
            })
            .subscribe();

        log.info("Notification consumer REACTIVE đã khởi động - đang lắng nghe topic banking-notification");
    }

    private String processNotification(String rawMessage) {
        try {
            JsonNode node = mapper.readTree(rawMessage);
            String msg = node.path("message").asText("Có giao dịch mới");
            String by  = node.path("initiatedBy").asText("unknown");
            String amt = node.path("amount").asText("0");
            String tid = node.path("transferId").asText();

            // Trong thực tế sẽ gửi Push Notification, SMS, Email...
            log.info("🔔 [NOTIFICATION] Gửi thông báo tới {}: {} - Số tiền: {} VND (transferId={})",
                by, msg, amt, tid);
            return tid;
        } catch (Exception e) {
            log.error("Lỗi parse notification: {}", e.getMessage());
            return "";
        }
    }
}
